from resources.lib.utils import (
    log, today, _requests,
    cacheMin, cacheHr
)
from resources.lib.vars import (
    IMG_QUALITY,
    MLB_API,
    NHL_API,
    MLB_FAV,
    NHL_FAV,
    SHOWMULTICAM,
)


class GameBuilder:
    @staticmethod
    def fromDate(date, remaining, provider, getfeeds):

        query = {'startDate': date, 'endDate': date}
        if provider == "NHL.tv":
            u = ''.join(NHL_API)
            query.update({
                'hydrate': 'linescore,game(content(editorial(preview),media(epg)))'
            })
            quality = {"Low":  "1136x640", "High": "1704x960", "Max":  "2568x1444"}
            size_n = quality.get(IMG_QUALITY, None)
        else:
            u = ''.join(MLB_API)
            query.update({
                # TODO: check editorial(all) during regular season
                'hydrate': 'linescore,probablePitcher,weather,game(content(media(epg)))',
                'sportId': '1'
            })
            # quality = {"Low":  3, "High": 1, "Max": 0}  # 960x540 1280x720 1920x1080
            # size_m = quality.get(IMG_QUALITY, None)

        # only use short cache for live games
        cache = cacheMin if today().strftime("%Y-%m-%d") == date else cacheHr
        response = _requests(cache).get(u, params=query, timeout=3)
        data = response.json()

        if data['totalItems'] <= 0 or len(data['dates']) == 0:
            return []
        games = data['dates'][0]['games']

        def asGame(g):
            thumb = ""
            desc = ""
            homeFull = ""
            awayFull = ""
            content = g['content']['link']
            away = g['teams']['away']['team']
            home = g['teams']['home']['team']
            time = g['gameDate'][11:].replace("Z", "")
            state = g['status']['detailedState']

            # without this check, querying this data happens again when all we want is the feed id
            if not getfeeds:
                if provider == "MLB.tv":
                    homeFull = f"[COLOR red][B]{home['name']}[/B][/COLOR]" if MLB_FAV in home['name'] else home['name']
                    awayFull = f"[COLOR red][B]{away['name']}[/B][/COLOR]" if MLB_FAV in away['name'] else away['name']

                    # reverse names from {last, first}
                    awayPitcher = g['teams']['away'].get("probablePitcher", {}).get("fullName", "unknown").split(', ')[::-1]
                    homePitcher = g['teams']['home'].get("probablePitcher", {}).get("fullName", "unknown").split(', ')[::-1]

                    condition = g['weather'].get('condition', 'N/A')
                    wind = g['weather'].get('wind', '').replace(',', ' winds')
                    temp = g['weather'].get('temp', '')

                    desc = (
                        f"[B]Starting Pitchers:[/B] \n"
                        f"{' '.join(awayPitcher)} vs. {' '.join(homePitcher)} \n\n"
                        f"[B]Weather:[/B] {condition} {temp}° \n"
                        f"{wind}"
                    )

                    # TODO: need to make this request smaller or async
                    # if size_m is not None:
                    #     # ~150kB * x games = ~3mB
                    #     response = _requests().get(MLB_API[0] + content, timeout=3).json()
                    #     try:
                    #         thumb = response['highlights']['highlights']['items'][0]['image']['cuts'][size_m]['src']
                    #     except LookupError:
                    #         pass
                else:
                    homeFull = f"[COLOR red][B]{home['name']}[/B][/COLOR]" if NHL_FAV in home['name'] else home['name']
                    awayFull = f"[COLOR red][B]{away['name']}[/B][/COLOR]" if NHL_FAV in away['name'] else away['name']

                    response = _requests().get(NHL_API[0] + content, timeout=3)
                    try:
                        desc = response.json()['editorial']['preview']['items'][0]['seoDescription']
                        if size_n is not None:
                            thumb = response.json()['editorial']['preview']['items'][0]['media']['image']['cuts'][size_n]['src']
                    except LookupError:
                        pass

            return Game(
                g['gamePk'],
                time,
                desc,
                thumb,
                state,
                awayFull,
                homeFull,
                remaining(state, g, provider),
                FeedBuilder.fromContent(g['content'], provider),
            )

        return [asGame(x) for x in games]

    @staticmethod
    def Remaining(state, game, provider):
        # contrary to nhl, mlb games get listed as pregame hours
        # before what we want to see, which is the warmup state
        state = "Scheduled" if provider == "MLB.tv" and state == "Pre-Game" else state
        if "In Progress" in state:
            idx = ['currentPeriodOrdinal', 'currentPeriodTimeRemaining'] if provider == "NHL.tv" else \
                  ['currentInningOrdinal', 'inningHalf']
            return f"{game['linescore'][idx[0]]} {game['linescore'][idx[1]]}"
        if state in ("Pre-Game", "Warmup"):
            return "Pre Game"
        return state


class FeedBuilder:
    @staticmethod
    def fromContent(content, provider):
        def idProvider(item):
            return item[mediaIdx]

        def fromItem(item):
            mediaFeedType = item['mediaFeedType'].upper()
            if mediaFeedType == "HOME":
                return Home(item['callLetters'], idProvider(item))
            if mediaFeedType == "AWAY":
                return Away(item['callLetters'], idProvider(item))
            if mediaFeedType == "NATIONAL":
                return National(item['callLetters'], idProvider(item))
            if mediaFeedType == "FRENCH":
                return French(item['callLetters'], idProvider(item))
            if mediaFeedType == "COMPOSITE" and SHOWMULTICAM:
                return Composite(item['callLetters'], idProvider(item))
            if mediaFeedType == "ISO":
                return Other(item['feedName'], item['callLetters'], idProvider(item))
            return NonViewable(item['callLetters'], idProvider(item))

        if "media" in content:
            streamProvider = provider.replace(".", "").upper()
            mediaIdx = "mediaPlaybackId" if provider == "NHL.tv" else "id"
            try:
                return [
                    fromItem(item)
                    for stream in content['media']['epg'] if stream['title'] == streamProvider
                    for item in stream['items']
                ]
            except KeyError:
                log("Found game with no feeds", debug=True)
        return []


class Feed:
    def __init__(self, tvStation, mediaId):
        self._tvStation = tvStation
        self._mediaId = mediaId

    def viewable(self):
        return True

    @property
    def tvStation(self):
        return self._tvStation

    @property
    def mediaId(self):
        return self._mediaId


class NonViewable(Feed):
    def __init__(self, tvStation, mediaId):
        Feed.__init__(self, tvStation, mediaId)

    def __repr__(self):
        return "NonViewable"

    def viewable(self):
        return False


class Home(Feed):
    def __init__(self, tvStation, mediaId):
        Feed.__init__(self, tvStation, mediaId)

    def __repr__(self):
        return f"{self.tvStation} (Home)"


class Away(Feed):
    def __init__(self, tvStation, mediaId):
        Feed.__init__(self, tvStation, mediaId)

    def __repr__(self):
        return f"{self.tvStation} (Away)"


class National(Feed):
    def __init__(self, tvStation, mediaId):
        Feed.__init__(self, tvStation, mediaId)

    def __repr__(self):
        return f"{self.tvStation} (National)"


class French(Feed):
    def __init__(self, tvStation, mediaId):
        Feed.__init__(self, tvStation, mediaId)

    def __repr__(self):
        return f"{self.tvStation} (French)"


class Composite(Feed):
    def __init__(self, tvStation, mediaId):
        Feed.__init__(self, tvStation, mediaId)

    def __repr__(self):
        return "3-Way Camera"


class Other(Feed):
    def __init__(self, feedName, tvStation, mediaId):
        Feed.__init__(self, tvStation, mediaId)
        self._feedName = feedName

    def __repr__(self):
        return self._feedName


class Game:
    def __init__(
        self,
        gid,
        time,
        desc,
        thumb,
        gameState,
        awayFull,
        homeFull,
        remaining,
        feeds=[],
    ):
        self._id = gid
        self._time = time
        self._desc = desc
        self._thumb = thumb
        self._gameState = gameState
        self._awayFull = awayFull
        self._homeFull = homeFull
        self._remaining = remaining
        if feeds is None:
            self._feeds = []
        else:
            self._feeds = feeds

    @property
    def gid(self):
        return self._id

    @property
    def time(self):
        return self._time

    @property
    def desc(self):
        return self._desc

    @property
    def thumb(self):
        return self._thumb

    @property
    def gameState(self):
        return self._gameState

    @property
    def awayFull(self):
        return self._awayFull

    @property
    def homeFull(self):
        return self._homeFull

    @property
    def remaining(self):
        return self._remaining

    @property
    def feeds(self):
        return self._feeds

    def __repr__(self):
        return "Game({} vs. {}, {}, feeds: {})".format(
            self.awayFull,
            self.homeFull,
            self.remaining,
            ", ".join([f.tvStation for f in self.feeds]),
        )
