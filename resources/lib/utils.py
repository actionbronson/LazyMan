import calendar
import random
import socket
import sys
import time
from datetime import datetime, timedelta
from pytz import reference, timezone
from requests.adapters import HTTPAdapter
from requests.packages.urllib3.util.retry import Retry

import requests
import requests_cache

import xbmc
import xbmcgui
import xbmcplugin
from resources.lib.vars import (
    ADDONHANDLE,
    CACHE,
    DEBUG,
    HEADERS,
    TIME_FRMT,
)


# cache epg listing for 1 minutes (this seems like a good medium between speed and stale data)
cacheMin = requests_cache.CachedSession(CACHE, backend='sqlite', fast_save=True, expire_after=60)
# cache individual game data for 1 hour (this request is substantially larger than the main epg list)
cacheHr  = requests_cache.CachedSession(CACHE + '-hourly', backend='sqlite', fast_save=True, expire_after=3600)
# cache larger requests for 12 hours
cacheDay = requests_cache.CachedSession(CACHE + '-daily', backend='sqlite', fast_save=True, expire_after=3600*12)


def _requests(session=cacheHr, retries=2):
    retry = Retry(
        total=retries,
        read=retries,
        connect=retries,
        backoff_factor=0.5,
        status_forcelist=(500, 502, 504),
    )
    adapter = HTTPAdapter(max_retries=retry)
    session.mount('http://', adapter)
    session.mount('https://', adapter)
    session.headers.update(HEADERS)
    return session


def log(message, debug=False):
    if debug is True and DEBUG is False:
        return
    level = xbmc.LOGNOTICE
    xbmc.log(f"LazyMan: {message}", level=level)


def asCurrentTz(d, t):
    parsed = datetime(*(time.strptime(f"{d} {t}", '%Y-%m-%d %H:%M:%S')[0:6]))
    replaced = parsed.replace(tzinfo=timezone("UTC"))
    local = replaced.astimezone(reference.LocalTimezone())
    return f"{local.strftime(TIME_FRMT)}"


def years(provider):
    start = 2015 if provider == "NHL.tv" else 2017
    return [*range(start, today().year + 1)]


def months(year):
    if int(year) == today().year:
        return [(calendar.month_name[m], m) for m in range(1, today().month + 1)]
    return [(calendar.month_name[m], m) for m in range(1, 13)]


def days(year, month):
    if int(year) == today().year and int(month) == today().month:
        return [*range(1, today().day)]
    r = calendar.monthrange(int(year), int(month))
    return [*range(1, max(r) + 1)]


def today(delta=0):
    date = datetime.now() - timedelta(delta)
    # this is the timezone the api is based on
    tz = timezone("America/Los_Angeles")
    return tz.localize(date)


def garble(s="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"):
    return "".join(random.choices(s, k=62))


def salt():
    garbled = garble()
    return "".join([garbled[int(i * random.random()) % len(garbled)] for i in range(0, 241)])


def head(url, cookies=None):
    ret = requests.head(url, cookies=cookies, timeout=3)
    return ret.status_code < 400


def resolve(host):
    try:
        return socket.gethostbyname(host)
    except socket.gaierror as e:
        log(f"Cannot resolve {host}: {e}")


def isUp(ip, port=80):
    timeout = 5
    try:
        s = socket.create_connection((ip, port), timeout)
        s.shutdown(socket.SHUT_RDWR)
        s.close()
        return True
    except OSError as e:
        log(f"Cannot connect to {ip}: {e}")
        return False


def add_list(name, action,
    provider=None,
    year=None,
    month=None,
    day=None,
    date=None,
    gid=None,
    state=None,
    url=None,
    desc=None,
    icon=None,
    group=None,
    isStream=False,
):
    u = f"{sys.argv[0]}?action={action}&mode={provider}"

    if year is not None:
        u += f"&year={str(year)}"
    if month is not None:
        u += f"&month={str(month)}"
    if day is not None:
        u += f"&day={str(day)}"
    if date is not None and gid is not None:
        u += f"&date={str(date)}&gid={str(gid)}"
    if state is not None:
        u += f"&state={state}"
    if url is not None:
        u += f"&url={url}"
    if group is not None:
        u += f"&group={group}"

    isFolder = True
    listItem = xbmcgui.ListItem(str(name))
    if icon is not None:
        if action == "playgame":
            listItem.setArt({'thumb': icon})
        elif action == "listtodaysgames":
            listItem.setArt({'fanart': icon})
        else:
            listItem.setArt({'thumb': icon, 'fanart': icon})

    listItem.setInfo(type="video", infoLabels={"title": name})
    if desc is not None:
        listItem.setInfo("video", {"title": name, "plot": desc})

    if isStream:
        listItem.setProperty("IsPlayable", "true")
        isFolder = False

    xbmcplugin.setContent(ADDONHANDLE, 'videos')
    return xbmcplugin.addDirectoryItem(ADDONHANDLE, url=u, listitem=listItem, isFolder=isFolder)


# from timeit import default_timer as timer
# start = timer()
# end = timer()
# log(end - start)
