import calendar
import configparser
import os
import re
import sys
import time
from urllib.parse import parse_qsl, quote

import requests
import requests_cache

import xbmc
import xbmcaddon
import xbmcgui
import xbmcplugin
from resources.lib import utils
from resources.lib.game import GameBuilder
from resources.lib.highlights import get_highlights
from resources.lib.utils import log


ADDONURL     = sys.argv[0]
ADDONHANDLE  = int(sys.argv[1])
ADDON        = xbmcaddon.Addon()
ADDONNAME    = ADDON.getAddonInfo('name')
ADDONPATH    = ADDON.getAddonInfo('path')
ICON         = ADDON.getAddonInfo('icon')
BASE_URL     = "freegamez.ga"
USER_AGENT   = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.116 Safari/537.36"

CONFIG = configparser.ConfigParser()
CONFIG.read(os.path.join(ADDONPATH, 'resources', 'lazyman.ini'))

CACHEPATH = os.path.join(ADDONPATH, 'resources', 'cache')
requests_cache.install_cache(CACHEPATH, backend='sqlite', expire_after=90)

items = []

def create_listitem(label):
    return xbmcgui.ListItem(label=str(label), offscreen=True)

def create_dir(list):
    xbmcplugin.addDirectoryItems(ADDONHANDLE, list, len(list))
    xbmcplugin.endOfDirectory(ADDONHANDLE, cacheToDisc=False)

def games(date, provider):
    return GameBuilder.fromDate(CONFIG, date, GameBuilder.Remaining, provider)

def listgrouphighlights(provider, group):
    for hg in [x for x in get_highlights(CONFIG, provider) if x.title == group]:
        for h in hg.highlights:
            label = f"{h.blurb} ({h.duration})"
            listItem = create_listitem(label)
            listItem.setInfo(type="video", infoLabels={"title": label, "plot": h.desc})
            listItem.setArt({'thumb': h.thumb})
            url = f"{ADDONURL}?action=playhighlight&url={h.playbackUrl}"
            items.append((url, listItem, True))
    xbmcplugin.setContent(ADDONHANDLE, 'videos')
    create_dir(items)

def listhighlights(provider):
    for hg in get_highlights(CONFIG, provider):
        listItem = create_listitem(hg.title)
        url = f"{ADDONURL}?action=listgrouphighlights&group={hg.title}&provider={provider}"
        items.append((url, listItem, True))
    create_dir(items)

def listyears(provider):
    for y in utils.years(provider):
        listItem = create_listitem(y)
        url = f"{ADDONURL}?action=listmonths&year={y}&provider={provider}"
        items.append((url, listItem, True))
    create_dir(items)

def listmonths(year, provider):
    for (mn, m) in utils.months(year):
        listItem = create_listitem(mn)
        url = f"{ADDONURL}?action=listdays&year={year}&month={m}&provider={provider}"
        items.append((url, listItem, True))
    create_dir(items)

def listdays(year, month, provider):
    for d in utils.days(year, month):
        listItem = create_listitem(d)
        url = f"{ADDONURL}?action=listgames&year={year}&month={month}&day={d}&provider={provider}"
        items.append((url, listItem, True))
    create_dir(items)

def listproviders():
    providers = CONFIG.get("LazyMan", "Providers").split(",")
    for provider in providers:
        listItem = create_listitem(provider)
        url = f"{ADDONURL}?action=listtodaysgames&provider={provider}"
        items.append((url, listItem, True))
    create_dir(items)

def listgames(date, provider, previous=False, highlights=False):
    dategames = games(date, provider)

    if len(dategames) == 0:
        xbmcplugin.endOfDirectory(ADDONHANDLE, succeeded=False)
        xbmcgui.Dialog().ok(ADDONNAME, "No games scheduled today")
        if not previous:
            return

    for g in dategames:
        label = f"{g.awayFull} vs. {g.homeFull} " \
                f"[{g.remaining if g.remaining != 'N/A' else utils.asCurrentTz(date, g.time)}]"
        listItem = create_listitem(label)
        listItem.setInfo(type="video", infoLabels={"title": label, "mediatype": 'video'})
        url = f"{ADDONURL}?action=feeds&game={g.id}&date={date}&provider={provider}"
        items.append((url, listItem, True))

    if highlights:
        listItem = create_listitem('Highlights')
        listItem.setInfo(type="video", infoLabels={"title": "Highlights", "mediatype": 'video'})
        url = f"{ADDONURL}?action=listhighlights&provider={provider}"
        items.append((url, listItem, True))

    if previous:
        listItem = create_listitem('Previous')
        listItem.setInfo(type="video", infoLabels={"title": "Previous", "mediatype": 'video'})
        url = f"{ADDONURL}?action=listyears&provider={provider}"
        items.append((url, listItem, True))
    create_dir(items)

def listfeeds(game, date, provider):
    def getfeedicon(feed):
        feed = p.sub('', feed)
        log(f"FeedIcon: {feed}", debug=True)
        return os.path.join(ADDONPATH, 'resources', 'icons', feed + '.png')

    p = re.compile('\ \(Home\)|\ \(Away\)|\ \(National\)+|\ \(French\)+|\ Camera|2|\+|\-')
    for f in [f for f in game.feeds if f.viewable()]:
        label = str(f)
        listItem = create_listitem(label)
        listItem.setInfo(type="video", infoLabels={"title": label, "mediatype": 'video'})
        icon = getfeedicon(label)
        listItem.setArt({'icon': icon})
        url = f"{ADDONURL}?action=play&date={date}&feedId={f.mediaId}&provider={provider}&state={game.gameState}"
        items.append((url, listItem, False))
    xbmcplugin.setContent(ADDONHANDLE, 'videos')
    create_dir(items)

def playhighlight(url):
    xbmc.Player().play(f"{url}|User-Agent={quote(USER_AGENT)}")
    xbmcplugin.endOfDirectory(ADDONHANDLE, succeeded=False)

def playgame(date, feedId, provider, state):
    def adjustQuality(masterUrl):
        qualityUrlDict = {
            "540p":   "2500K/2500_{0}.m3u8",
            "720p":   "3500K/3500_{0}.m3u8",
            "720p60": "5600K/5600_{0}.m3u8"
        }
        current = ADDON.getSetting("quality")
        if current == 'master':
            return masterUrl

        log(f"StreamQualityWanted: {current}", debug=True)
        ext_live = 'slide' if provider == 'NHL.tv' else 'complete'
        m3u8Path = qualityUrlDict.get(current, "3500K/3500_{0}.m3u8").format(
            ext_live if state in ('In Progress', 'Scheduled', 'Pre-Game', 'Warmup')
            else 'complete-trimmed')

        log(f"AdjustedQuality: {m3u8Path}", debug=True)
        return masterUrl.rsplit('/', 1)[0] + "/" + m3u8Path

    log(f"GameState: {state}", debug=True)
    cdn = 'akc' if ADDON.getSetting("cdn") == 'Akamai' else 'l3c'
    url = f"http://{BASE_URL}/mlb/m3u8/{date}/{feedId}{cdn}"
    contentUrl = url.replace('mlb/', '') if provider == 'NHL.tv' else url

    log(f"Checking contentUrl: {contentUrl}", debug=True)
    if not utils.head(contentUrl):
        log("Invalid contentUrl")
        xbmcgui.Dialog().ok(ADDONNAME, "Game not available yet")
        return

    url = requests.get(contentUrl).text
    log(f"Stream URL resolved: {url}", debug=True)

    cookie = utils.salt()
    auth_header = f"mediaAuth%%3D%%22{cookie}%%22"

    if not utils.head(url, dict(mediaAuth=cookie)):
        xbmcgui.Dialog().ok(ADDONNAME, "Error while contacting server", "Try switching CDN and try again")
        return
    xbmc.Player().play(f"{adjustQuality(url)}|Cookie={auth_header}&User-Agent={quote(USER_AGENT)}")

def router(paramstring):
    params = dict(parse_qsl(paramstring))
    if params:
        if params['action'] == 'feeds':
            dategames = games(params['date'], params['provider'])
            gameDict = dict([(g.id, g) for g in dategames])
            listfeeds(gameDict[int(params['game'])], params['date'], params['provider'])
        elif params['action'] == 'play':
            playgame(params['date'], params['feedId'], params['provider'], params['state'])
        elif params['action'] == 'listyears':
            listyears(params['provider'])
        elif params['action'] == 'listhighlights':
            listhighlights(params['provider'])
        elif params['action'] == 'listgrouphighlights':
            listgrouphighlights(params['provider'], params['group'])
        elif params['action'] == 'playhighlight':
            playhighlight(params['url'])
        elif params['action'] == 'listmonths':
            listmonths(params['year'], params['provider'])
        elif params['action'] == 'listdays':
            listdays(params['year'], params['month'], params['provider'])
        elif params['action'] == 'listgames':
            listgames(f"{int(params['year'])}-{int(params['month']):02}-{int(params['day']):02}", params['provider'])
        elif params['action'] == 'listtodaysgames':
            listgames(utils.today().strftime("%Y-%m-%d"), params['provider'], True, True)
    else:
        listproviders()

def sanityCheck():
    # time (in hours) between checking dns entries
    if calendar.timegm(time.gmtime()) - (3600 * 24) > ADDON.getSettingInt("sanityChecked"):
        providers = CONFIG.get("LazyMan", "Providers").split(",")
        for service in providers:
            hostNames = CONFIG.get(service, "Host").split(",")
            lazymanServer = utils.resolve(BASE_URL)
            # check if server is alive
            if not utils.isUp(lazymanServer):
                xbmcgui.Dialog().ok(ADDONNAME, "The Lazyman Server is Offline.")
                break
            xbmc.executebuiltin(f"Notification(LazyMan,Verifying {service},,{ICON})")
            for host in hostNames:
                # check if dns entries are redirected properly
                resolved = utils.resolve(host)
                if resolved != lazymanServer:
                    xbmcgui.Dialog().ok(ADDONNAME, f"{host} doesn't resolve to the Lazyman server.",
                                                   f"Update your hosts file to point to {lazymanServer}")
                else:
                    ADDON.setSettingInt("sanityChecked", calendar.timegm(time.gmtime()))


if __name__ == '__main__':
    sanityCheck()
    router(sys.argv[2][1:])
