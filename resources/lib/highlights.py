from configparser import NoOptionError

import requests
from .utils import log


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
            desc = h['description']
            thumb = h['image']['cuts']['640x360']['src']
            playbacks = [x for x in h['playbacks'] if x['name'] == 'HTTP_CLOUD_WIRED_60']
            if len(playbacks) > 0:
                title_highlights.append(Highlight(blurb, duration, playbacks[0]['url'], thumb, desc))
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
    thumb = None
    desc = None
    def __init__(self, blurb_, duration_, playback_, thumb_, desc_):
        self.blurb = blurb_
        self.duration = duration_
        self.playbackUrl = playback_
        self.thumb = thumb_
        self.desc = desc_

    def viewable(self):
        return True
