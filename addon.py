import os
import re
import sys
import time
from urllib.parse import parse_qsl, quote

import xbmc
import xbmcgui
import xbmcplugin
#import youtube_requests

from resources.lib import highlights
from resources.lib import utils
from resources.lib.game import GameBuilder
from resources.lib.utils import log, _requests, add_list
from resources.lib.vars import (
    ADDON,
    ADDONID,
    ADDONNAME,
    ADDONHANDLE,
    BASE_URL,
    CDN,
    ICON,
    IMG_QUALITY,
    INPUTSTREAM,
    SHOWALLDAYS,
    STRM_QUALITY,
    USER_AGENT,
)


def play(url, mode=None, highlight=False):
    item = xbmcgui.ListItem(path=url)
    item.setMimeType('application/x-mpegURL')

    if mode == "youtube":
        url = f"plugin://plugin.video.youtube/play/?incognito=true&video_id={url}"
        item.setPath(url)
    else:
        auth_header = f"mediaAuth%3D%22{utils.salt()}%22"
        url = f"{url}|cookie={auth_header}&user-agent={quote(USER_AGENT)}"
        # TODO: inputstream still verifies ssl cert
        if INPUTSTREAM or highlight:
            item.setPath(url.split('|')[0])
            item.setProperty('inputstreamaddon', 'inputstream.adaptive')
            item.setProperty('inputstream.adaptive.manifest_type', 'hls')
            item.setProperty('inputstream.adaptive.stream_headers', url.split('|')[1])

    xbmcplugin.setResolvedUrl(ADDONHANDLE, True, item)


def youtube(u):
    playlist = youtube_requests.get_playlist_items(u)
    # dont show deleted videos
    for vid in [x for x in playlist[:-1] if "Deleted" not in x['snippet']['title']]:
        title = vid['snippet']['title']
        url = vid['snippet']['resourceId']['videoId']
        quality = "maxres" if IMG_QUALITY in ("High", "Max") else "standard"
        thumb = vid['snippet']['thumbnails'][quality]['url']
        desc = vid['snippet']['description']
        # shorten title and description
        title = re.sub(
            r"(NHL Highlights \| |Game Highlights)", '', title,
            flags=re.DOTALL)
        desc = re.sub(
            r"(Don't forget to subscribe|For the latest hockey action).*", '', desc,
            flags=re.DOTALL)
        add_list(title, "playhighlight", 'youtube', url=url, icon=thumb, desc=desc, isStream=True)


def games(date, provider, getfeeds=False):
    return GameBuilder.fromDate(date, GameBuilder.Remaining, provider, getfeeds)


def list_year(provider):
    for y in utils.years(provider):
        add_list(y, "listmonths", provider, year=y)


def list_month(y, provider):
    for (mn, m) in utils.months(y):
        add_list(mn, "listdays", provider, year=y, month=m)


def list_day(y, m, provider):
    for d in utils.days(y, m):
        add_list(d, "listgames", provider, year=y, month=m, day=d)


def menu():
    for provider in ("NHL.tv", "MLB.tv"):
        leaders = highlights.get_leaders(provider)
        thumb = highlights.random_image(provider)
        add_list(provider, 'listtodaysgames', provider, desc=leaders, icon=thumb)


def list_games(date, provider, previous=False, highlights=False):
    dategames = games(date, provider)

    if len(dategames) < 1:
        xbmcplugin.endOfDirectory(ADDONHANDLE, succeeded=False)
        xbmcgui.Dialog().ok(ADDONNAME, "No games scheduled today")
        if not previous:
            return

    for g in dategames:
        label = (
            f"{g.awayFull} vs. {g.homeFull} "
            f"[{g.remaining if g.remaining != 'Scheduled' else utils.asCurrentTz(date, g.time)}]"
        )
        add_list(label, 'listfeeds', provider, date=date, gid=g.gid, desc=g.desc, icon=g.thumb)

    if previous:
        action = "yesterday"
        title = "[I]Yesterdays Games[/I]"
        if SHOWALLDAYS:
            action = "listyears"
            title = "[I]Previous Games[/I]"
        add_list(title, action, provider)

    if highlights:
        add_list("[I]Highlights[/I]", "listhighlightsgroup", provider)


def list_feeds(game, date, provider):
    def getfeedicon(feed):
        feed = p.sub("", feed)
        log(f"FeedIcon: {feed}", debug=True)
        return os.path.join('special://home', 'addons', ADDONID, 'resources', 'icons', feed + '.png')

    p = re.compile(r" \((Home|Away|National|French|)\)|\.(com|tv)| Camera|2|[ +-]")
    for f in [f for f in game.feeds if f.viewable()]:
        label = str(f)
        icon = getfeedicon(label)
        log(f"FeedIcon: {icon}", debug=True)
        add_list(label, "playgame", provider, date=date, gid=f.mediaId, state=game.gameState, icon=icon, isStream=True)


def highlights_menu(provider):
    if provider == "NHL.tv":
        # TODO: this gets called (v) twice... rewrite it
        for hg in highlights.get_nhl_highlights():
            add_list(hg.title, "listhighlights", provider, group=hg.title)
    add_list("Game Recaps", "listrecaps", provider, state=1)  # state=page
    add_list("Team Videos", "listteams", provider)
    #else:
    #add_list("Game Recaps (youtube)", "get_youtubePlaylist", YT_RECAP[provider])


def list_highlights(provider, group):
    for hg in [x for x in highlights.get_nhl_highlights() if x.title == group]:
        for h in hg.highlights:
            label = f"{h.blurb} ({h.duration})"
            add_list(label, "playhighlight", url=h.playbackUrl, desc=h.desc, icon=h.thumb, isStream=True)


def get_stream(date, feed, provider, state):
    def adjustQuality(masterUrl):
        quality = {'540p':   '2500K/2500_{0}.m3u8',
                   '720p':   '3500K/3500_{0}.m3u8',
                   '720p60': '5600K/5600_{0}.m3u8'
        }
        if STRM_QUALITY == "master":
            return masterUrl

        ext_live = "slide" if provider == "NHL.tv" else "complete"
        m3u8Path = quality.get(STRM_QUALITY).format(
            ext_live
            if state in ("In Progress", "Scheduled", "Pre-Game", "Warmup")
            else "complete-trimmed"
        )

        log(f"AdjustedQuality: {m3u8Path}", debug=True)
        return f"{masterUrl.rsplit('/', 1)[0]}/{m3u8Path}"

    log(f"GameState: {state}", debug=True)
    url = f"https://{BASE_URL}/mlb/m3u8/{date}/{feed}{CDN}"
    contentUrl = url.replace('mlb/', '') if provider == "NHL.tv" else url

    log(f"Checking contentUrl: {contentUrl}", debug=True)
    if not utils.head(contentUrl):
        log("Invalid contentUrl")
        xbmcplugin.endOfDirectory(ADDONHANDLE, succeeded=False)
        xbmcgui.Dialog().ok(ADDONNAME, "Game not available yet")
        return

    url = _requests().get(contentUrl, timeout=3).text
    log(f"Stream URL resolved: {url}", debug=True)

    if not utils.head(url):
        xbmcplugin.endOfDirectory(ADDONHANDLE, succeeded=False)
        xbmcgui.Dialog().ok(ADDONNAME, "Stream is unavailable")
        return
    play(adjustQuality(url))


def dnsCheck():
    # time (in hours) between checking dns entries
    if int(time.time() - (24 * 3600)) > ADDON.getSettingInt("dnsChecked"):
        lazymanServer = utils.resolve(BASE_URL)
        # check if server is alive
        if not lazymanServer or not utils.isUp(lazymanServer):
            xbmcgui.Dialog().ok(ADDONNAME, "The Lazyman Server is Offline.")
            return
        xbmc.executebuiltin(f"Notification(LazyMan,Checking DNS...,,{ICON})")
        for host in (
            "mf.svc.nhl.com",
            "mlb-ws-mf.media.mlb.com",
            "playback.svcs.mlb.com",
        ):
            # check if dns entries are redirected properly
            resolved = utils.resolve(host)
            if resolved != lazymanServer:
                xbmcgui.Dialog().ok(
                    ADDONNAME,
                    f"{host} doesn't resolve to the Lazyman server.",
                    f"Update your hosts file to point to {lazymanServer}",
                )
            else:
                ADDON.setSettingInt("dnsChecked", int(time.time()))


params = dict(parse_qsl(sys.argv[2][1:]))
#log(f"params: {params}", debug=True)
action = params['action'] if 'action' in params else None
cacheToDisc = True

if action is None:
    dnsCheck()
    menu()

elif action == "listtodaysgames":
    list_games(utils.today().strftime("%Y-%m-%d"), params['mode'], True, True)
    cacheToDisc = False
elif action == "listgames":
    list_games(f"{int(params['year'])}-{int(params['month']):02}-{int(params['day']):02}", params['mode'])
elif action == "listfeeds":
    # TODO: pylint suggestion
    gameDict = dict([(g.gid, g) for g in games(params['date'], params['mode'], getfeeds=True)])
    list_feeds(gameDict[int(params['gid'])], params['date'], params['mode'])

elif action == "playgame":
    get_stream(params['date'], params['gid'], params['mode'], params['state'])
elif action == "playhighlight":
    play(params['url'], params['mode'], True)

elif action == "listhighlightsgroup":
    highlights_menu(params['mode'])
elif action == "listhighlights":
    list_highlights(params['mode'], params['group'])
elif action == "listrecaps":
    highlights.get_recaps(params['mode'], int(params['state']))
#elif action == "get_youtubePlaylist":
#    youtube(params['mode'])

elif action == "listteams":
    highlights.teamList(params['mode'])
elif action == "listteam":
    highlights.team(params['url'], params['mode'])
elif action == "listteam_subdir":
    highlights.teamSub(params['url'], params['mode'])

elif action == "listyears":
    list_year(params['mode'])
elif action == "listmonths":
    list_month(params['year'], params['mode'])
elif action == "listdays":
    list_day(params['year'], params['month'], params['mode'])
elif action == "yesterday":
    list_games(utils.today(1).strftime("%Y-%m-%d"), params['mode'], False, True)

xbmcplugin.endOfDirectory(ADDONHANDLE, cacheToDisc=cacheToDisc)
