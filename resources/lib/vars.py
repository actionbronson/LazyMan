import os
import sys
import xbmcaddon


ADDONURL     = sys.argv[0]
ADDONHANDLE  = int(sys.argv[1])
ADDON        = xbmcaddon.Addon()
ADDONNAME    = ADDON.getAddonInfo('name')
ADDONID      = ADDON.getAddonInfo('id')
ADDONPATH    = ADDON.getAddonInfo('path')
ICON         = ADDON.getAddonInfo('icon')
STRM_QUALITY = ADDON.getSetting('qualityStrm')
IMG_QUALITY  = ADDON.getSetting('qualityArt')
DEBUG        = ADDON.getSettingBool('debug')
INPUTSTREAM  = ADDON.getSettingBool('inputstream')
SHOWALLDAYS  = ADDON.getSettingBool('showAll')
SHOWMULTICAM = ADDON.getSettingBool('showMultiCam')

CDN          = "akc" if ADDON.getSetting('cdn') == "Akamai" else "l3c"
LANG         = ADDON.getLocalizedString
CACHE        = os.path.join(ADDONPATH, 'resources', 'cache')

BASE_URL     = "freegamez.ga"
USER_AGENT   = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36"
HEADERS      = {'User-Agent': USER_AGENT}

MLB_FAV      = int(f"{ADDON.getSettingInt('mlbFav'):02}")
NHL_FAV      = int(f"{ADDON.getSettingInt('nhlFav'):02}")

NHL_API      = ['https://statsapi.web.nhl.com', '/api/v1/schedule']
MLB_API      = ['https://statsapi.mlb.com', '/api/v1/schedule']

# https://www.youtube.com/user/NHLVideo/playlists
# https://www.youtube.com/user/MLB/playlists
# YT_RECAP     = {'NHL.tv': 'PL1NbHSfosBuHInmjsLcBuqeSV256FqlOO',
#                 'MLB.tv': 'PLL-lmlkrmJanFMn0_N4Hw0fci5OiaID6e'}
