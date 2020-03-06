import random
import re
from bs4 import BeautifulSoup

from resources.lib.utils import (
    log, _requests, today, add_list,
    cacheHr, cacheDay,
)
from resources.lib.vars import (
    IMG_QUALITY,
    LANG,
    NHL_API,
    NHL_FAV,
    MLB_API,
    MLB_FAV,
)


class HighlightGroup:
    highlights = []

    def __init__(self, title_, highlights_):
        self.title = title_
        self.highlights = highlights_


class Highlight:
    def __init__(self, blurb_, duration_, playback_, thumb_, desc_):
        self.blurb = blurb_
        self.duration = duration_
        self.playbackUrl = playback_
        self.thumb = thumb_
        self.desc = desc_

    def viewable(self):
        return True


def get_nhl_highlights():
    url = "https://nhl.bamcontent.com/nhl/en/nav/v1/video/connectedDevices/nhl/playstation-v1.json"
    data = _requests().get(url, timeout=3).json()

    highlights = []
    for topic in data['topics']:
        title = topic['title']
        title_highlights = []
        for video in topic['list']:
            blurb = video['blurb']
            duration = video['duration']
            desc = video['description']
            thumb = video['image']['cuts']['1136x640']['src'] if IMG_QUALITY != "Off" else ""
            playbacks = [x for x in video['playbacks'] if x['name'] == "HTTP_CLOUD_WIRED_60"][0]['url']
            title_highlights.append(Highlight(blurb, duration, playbacks, thumb, desc))
        highlights.append(HighlightGroup(title, title_highlights))
    return highlights


def get_recaps(provider, page):
    if provider == "NHL.tv":
        # Recaps endpoint is /gameRecap if this ever fails
        url = "https://search-api.svc.nhl.com/svc/search/v2/nhl_global_en/tag/content/extendedHighlights"
        query = {'page': page, 'sort': 'new', 'type': 'video'}
        data = _requests().get(url, params=query, timeout=3).json()

        for video in data['docs']:
            content = video['asset_id']
            url = f"https://nhl.bamcontent.com/nhl/id/v1/{content}/details/web-v1.json"
            data = _requests().get(url, timeout=3).json()
            #date = [x for x in data['keywordsAll'] if x['type'] == 'gameId'][0]['displayName'].split('-')[1]
            title = [x for x in data['keywordsAll'] if x['type'] == "calendarEventId"][0]['displayName']
            #duration = data['duration']
            #title = f"{name} [{duration}]"
            desc = data['bigBlurb']
            thumb = data['image']['cuts']['1136x640']['src'] if IMG_QUALITY != "Off" else ""
            url = [x for x in data['playbacks'] if x['name'] == "HTTP_CLOUD_WIRED_60"][0]['url']
            add_list(title, "playhighlight", url=url, desc=desc, icon=thumb, isStream=True)

    elif provider == "MLB.tv":
        # show today then yesterday on page 2
        # TODO: check if this is too many when season starts
        start = 1 if page == 1 else 3
        end   = 0 if page == 1 else 2

        query = {
            'sportId': '1',
            'startDate': today(start).strftime("%Y-%m-%d"),
            'endDate': today(end).strftime("%Y-%m-%d")
        }
        u = ''.join(MLB_API)
        gamelist = _requests(cacheHr).get(u, params=query, timeout=3).json()

        if len(gamelist) < 1:
            return

        quality = {'Low': '960', 'High': '1280', 'Max': '1920'}
        size = quality.get(IMG_QUALITY, '640')

        for d in reversed(gamelist['dates']):
            for g in d['games']:
                # ~200kB
                data = _requests().get(MLB_API[0] + g['content']['link'], timeout=3).json()

                # check for 8m condensed games first then 4m recaps if not found
                try:
                    highlight = data['media']['epgAlternate'][0]['items'][0]
                except:
                    try:
                        highlight = data['media']['epgAlternate'][1]['items'][0]
                    except:
                        log("no recap found", debug=True)
                        continue

                title = re.sub(r"(CG:|\|) ", '', highlight['title'])
                desc = highlight['blurb']
                #duration = highlight['duration']
                #title = f"{name} [{duration}]"
                # NOTE: if this fails, check if 'width' is a string or int
                # TODO: when season starts, use recap image instead
                thumb = [x for x in highlight['image']['cuts'] if x['width'] == int(size)][0]['src'] if IMG_QUALITY != "Off" else ""
                # TODO: when season starts, check m3u8
                url = [x for x in highlight['playbacks'] if x['name'] == 'HTTP_CLOUD_WIRED_60'][0]['url']
                add_list(title, "playhighlight", url=url, desc=desc, icon=thumb, isStream=True)

    if page == 1:
        add_list("[I]Next Page[/I]", "listrecaps", provider, state=2)


def teamList(provider):
    if provider == "NHL.tv":
        url = f"{NHL_API[0]}/api/v1/teams"
        data = _requests().get(url, timeout=3).json()

        for item in data['teams']:
            team = item['teamName']
            city = item['locationName']
            url = f"https://www.nhl.com/{team.lower().replace(' ', '')}/video"
            title = f"[COLOR red][B]{city} {team}[/B][/COLOR]" if LANG(30000 + NHL_FAV) in team else f"{city} {team}"
            add_list(title, "listteam", provider, url=url)

    elif provider == "MLB.tv":
        url = f"{MLB_API[0]}/api/v1/teams?sportId=1"
        data = _requests().get(url, timeout=3).json()

        for item in data['teams']:
            team = item['teamName']
            city = item['locationName']
            title = f"[COLOR red][B]{city} {team}[/B][/COLOR]" if LANG(40000 + MLB_FAV) in team else f"{city} {team}"
            url = f"https://www.mlb.com/{team.lower().replace('-', '').replace(' ', '')}/video"
            add_list(title, "listteam", provider, url=url)


def team(url, provider):
    if provider == "NHL.tv":
        data = _requests().get(url, timeout=3).text
        soup = BeautifulSoup(data, 'html.parser')

        for item in soup.find_all(attrs={'class': 'section-banner__tray-item'}):
            title = item.find('a').text
            url = f"https://www.nhl.com{item.find('a')['href']}"
            add_list(title, "listteam_subdir", provider, url=url)

    elif provider == "MLB.tv":
        data = _requests().get(url, timeout=3).text
        soup = BeautifulSoup(data, 'html.parser')

        # NOTE: look here if no results found
        for item in soup.find_all('a', attrs={'data-parent': 'Video'}):
            if "video/topic" not in item['href']:
                continue
            title = item.text
            topic = item['href'].split('/')[-1]
            add_list(title, "listteam_subdir", provider, url=topic)


def teamSub(url, provider):
    if provider == "NHL.tv":
        topic = url.split('t-')[1]
        url = f"https://search-api.svc.nhl.com/svc/search/v2/nhl_global_en/topic/{topic}"
        query = {'page': 1, 'sort': 'new', 'type': 'video'}
        # TODO: some team pages arent the same, wrap in try except block
        data = _requests().get(url, params=query, timeout=3).json()

        for item in data['docs']:
            content = item['url'].split('c-')[1]
            u = f"https://nhl.bamcontent.com/nhl/id/v1/{content}/details/web-v1.json"
            data = _requests().get(u, timeout=3).json()

            title = item['title']
            desc = data['description']
            url = [x for x in data['playbacks'] if x['name'] == 'HTTP_CLOUD_WIRED_60'][0]['url']
            try:
                # all videos dont all have the higher qualities
                thumb = data['image']['cuts']['1136x640']['src'] if IMG_QUALITY != "Off" else ""
            except KeyError:
                thumb = ""
                log(f"Failed to get thumbnail: {u}", debug=True)
            add_list(title, "playhighlight", url=url, desc=desc, icon=thumb, isStream=True)

    elif provider == "MLB.tv":
        url = f"https://www.mlb.com/data-service/en/topic/{url}"
        data = _requests().get(url, timeout=3).json()

        try:
            url = data['advancedCriteria']
            url = f"https://www.mlb.com/data-service/en/search?advancedCriteria={url}"
            key = 'docs'
        except:
            url = data['selection'][0]['slug']
            url = f"https://www.mlb.com/data-service/en/selection/{url}?$limit=15"
            key = 'items'

        data = _requests().get(url, timeout=3).json()

        for item in data[key]:
            title = item['title']
            desc = item['description']
            #duration = item['duration']
            #title = f"{name} [{duration}]"
            url = [x for x in item['playbacks'] if x['name'] == "HTTP_CLOUD_WIRED_60"][0]['url']
            thumb = item['image']['cuts'][4]['src'] if IMG_QUALITY != "Off" else ""  # 1536x864
            add_list(title, "playhighlight", url=url, desc=desc, icon=thumb, isStream=True)


def get_leaders(provider):
    desc = ""
    if provider == "NHL.tv":
        url = f"{NHL_API[0]}/api/v1/standings/byConference"
        # ~30kB
        data = _requests(cacheDay).get(url, timeout=3).json()

        for d in data['records']:
            title = d['conference']['name']
            desc += f"[B]{title} Conference:[/B]\n"
            for t in d['teamRecords'][:4]:
                title = t['team']['name']
                points = t['points']
                desc += f"  - {title} ({points})\n"

    elif provider == "MLB.tv":
        # NOTE: example queries
        #       leagueId=103,104 (american, national)
        #       season=2020&standingsTypes=regularSeason,springTraining,firstHalf,secondHalf
        #       hydrate=division,conference,sport,league,team(nextSchedule(team,gameType=[R,F,D,L,W,C],inclusive=false),previousSchedule(team,gameType=[R,F,D,L,W,C],inclusive=true))
        query = {
            'leagueId': '103,104',  # american, national
            'season': today().strftime("%Y"),
            # TODO: change when season starts
            'standingsTypes': 'springTraining',
            'hydrate': 'league'
        }
        url = f"{MLB_API[0]}/api/v1/standings"
        # ~150kB
        data = _requests(cacheDay).get(url, params=query, timeout=3).json()

        for league in ("American League", "National League"):
            desc += f"[B]{league}:[/B]\n"
            for d in [x for x in data['records'] if x['league']['name'] == league]:
                for t in sorted(d['teamRecords'], key=lambda x: x['divisionRank'], reverse=False)[:1]:
                    title = t['team']['name']
                    wins = t['winningPercentage']
                    desc += f"  - {title} ({wins})\n"
    return desc


def random_image(provider):
    if IMG_QUALITY == "Off":
        return None

    items = []
    if provider == "NHL.tv":
        topics = ['277729162', '278387726', '278387530', '277729400']
        url = f"https://www.nhl.com/news/t-{random.choice(topics)}"
        # ~900kB
        data = _requests(cacheDay).get(url, timeout=3).text
        soup = BeautifulSoup(data, 'html.parser')

        quality = {'Low': '1136', 'High': '2048', 'Max': '2568'}
        size = quality.get(IMG_QUALITY, '1136')

        for item in soup.find_all("img", class_="article-sidebar-item__img "):
            item = item.get("data-srcset").split(' ')
            # dont fail if the quality sizes change in the future
            try:
                # unlike mlb, the results arent sorted for easy pickings
                url = [x for x in item if size in x][0]
                items.append(url)
            except:
                log(f"Failed to find a team image: {size} from {item}", debug=True)

    elif provider == "MLB.tv":
        team = LANG(40000 + MLB_FAV).replace(' ', '').lower()
        url = f"https://www.mlb.com/{team}/team/photos" if team != "none" else \
               "https://www.mlb.com/mlb/team/photos/mlb-ballpark-sunsets"
        # ~1.2mB / ~400kB
        data = _requests(cacheDay).get(url, timeout=3).text
        soup = BeautifulSoup(data, 'html.parser')

        for item in soup.find_all("img", class_="lazyload"):
            # quality should always be 1284x722
            items.append(item.get("data-srcset").split(' ')[0])

    return random.choice(items) if len(items) > 0 else None
