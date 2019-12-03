import requests
from configparser import NoOptionError


def get_highlights(config, provider):
    data = None
    try:
        response = requests.get(config.get(provider, "HighlightsUrl"))
        data = response.json()
    except NoOptionError:
        return []
    highlights = []
    for t in data['topics']:
        title = t['title']
        title_highlights = []
        for h in t['list']:
            blurb = h['blurb']
            duration = h['duration']
            playbacks = [x for x in h['playbacks'] if x['name'] == 'HTTP_CLOUD_WIRED_60']
            if len(playbacks) > 0:
                #from .utils import log
                #log("Creating highlight [%s (%s)] from %s" % (blurb, duration, playbacks[0]['url']))
                title_highlights.append(Highlight(blurb, duration, playbacks[0]['url']))
        highlights.append(HighlightGroup(title, title_highlights))
    return highlights

class HighlightGroup:
    highlights = []
    title = None
    def __init__(self, title_, highlights_):
        self.title = title_
        self.highlights = highlights_

class Highlight:
    playbackUrl = None
    duration = None
    blurb = None
    def __init__(self, blurb_, duration_, playback_):
        self.blurb = blurb_
        self.duration = duration_
        self.playbackUrl = playback_

    def viewable(self):
        return True
