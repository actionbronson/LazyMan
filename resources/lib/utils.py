import calendar
import random
import time
from datetime import datetime, timedelta

import requests
import xbmc

from pytz import reference, timezone


losangeles = timezone('America/Los_Angeles')
localtz = reference.LocalTimezone()

def log(message):
    level=xbmc.LOGNOTICE
    xbmc.log("LazyMan: {0}".format(message), level=level)

def today(tz=losangeles):
    date = datetime.now()
    return tz.localize(date)

def asCurrentTz(d, t):
    parsed = None
    try:
        parsed = datetime.strptime(d + " " + t, '%Y-%m-%d %H:%M:%S')
    except TypeError:
        parsed = datetime(*(time.strptime(d + " " + t, '%Y-%m-%d %H:%M:%S')[0:6]))
    replaced = parsed.replace(tzinfo=timezone('UTC'))
    local = replaced.astimezone(localtz)
    return "%02d:%02d" % (local.hour, local.minute)

def years(provider):
    start = 2017 if provider == "MLB.tv" else 2015
    return list(range(start, today().year + 1))

def months(year):
    if int(year) == today().year:
        return [(calendar.month_name[m], m) for m in range(1, today().month + 1)]
    else:
        return [(calendar.month_name[m], m) for m in range(1, 13)]

def days(year, month):
    if int(year) == today().year and int(month) == today().month:
        return list(range(1, today().day))
    else:
        r = calendar.monthrange(int(year), int(month))
        return list(range(1, max(r)+1))

def garble(salt="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"):
    return ''.join(random.sample(salt, len(salt)))

def salt():
    garbled = garble()
    return ''.join([garbled[int(i * random.random()) % len(garbled)] for i in range(0, 241)])

def head(url, cookies=dict()):
    ret = requests.head(url, cookies=cookies)
    return ret.status_code < 400

def get(url, cookies=dict()):
    ret = requests.get(url, cookies=cookies)
    return ret.status_code < 400
