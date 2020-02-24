import calendar
import random
import socket
import time
from datetime import datetime

import requests

import xbmc
import xbmcaddon
from pytz import reference, timezone


TIMEZONE = timezone('America/Los_Angeles')
DEBUGLOG = xbmcaddon.Addon().getSettingBool("debug")

def log(message, debug=False):
    if debug is True and DEBUGLOG is False:
        return
    level = xbmc.LOGNOTICE
    xbmc.log(f"LazyMan: {message}", level=level)

def today(tz=TIMEZONE):
    date = datetime.now()
    return tz.localize(date)

def asCurrentTz(d, t):
    parsed = None
    try:
        parsed = datetime.strptime(d + " " + t, '%Y-%m-%d %H:%M:%S')
    except TypeError:
        parsed = datetime(*(time.strptime(d + " " + t, '%Y-%m-%d %H:%M:%S')[0:6]))
    replaced = parsed.replace(tzinfo=timezone('UTC'))
    local = replaced.astimezone(reference.LocalTimezone())
    return f"{local.hour:02}:{local.minute:02}"

def years(provider):
    start = 2017 if provider == "MLB.tv" else 2015
    return list(range(start, today().year + 1))

def months(year):
    if int(year) == today().year:
        return [(calendar.month_name[m], m) for m in range(1, today().month + 1)]
    return [(calendar.month_name[m], m) for m in range(1, 13)]

def days(year, month):
    if int(year) == today().year and int(month) == today().month:
        return list(range(1, today().day))
    r = calendar.monthrange(int(year), int(month))
    return list(range(1, max(r) + 1))

def garble(s="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"):
    return ''.join(random.choices(s, k=62))

def salt():
    garbled = garble()
    return ''.join([garbled[int(i * random.random()) % len(garbled)] for i in range(0, 241)])

def head(url, cookies=None):
    ret = requests.head(url, cookies=cookies)
    return ret.status_code < 400

def get(url, cookies=None):
    ret = requests.get(url, cookies=cookies)
    return ret.status_code < 400

def resolve(host):
    return socket.gethostbyname(host)

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

#from timeit import default_timer as timer
#start = timer()
#end = timer()
#log(end - start)
