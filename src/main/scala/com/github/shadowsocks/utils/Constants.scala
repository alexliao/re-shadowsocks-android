/*
 * Shadowsocks - A shadowsocks client for Android
 * Copyright (C) 2014 <max.c.lv@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *                            ___====-_  _-====___
 *                      _--^^^#####//      \\#####^^^--_
 *                   _-^##########// (    ) \\##########^-_
 *                  -############//  |\^^/|  \\############-
 *                _/############//   (@::@)   \\############\_
 *               /#############((     \\//     ))#############\
 *              -###############\\    (oo)    //###############-
 *             -#################\\  / VV \  //#################-
 *            -###################\\/      \//###################-
 *           _#/|##########/\######(   /\   )######/\##########|\#_
 *           |/ |#/\#/\#/\/  \#/\##\  |  |  /##/\#/  \/\#/\#/\#| \|
 *           `  |/  V  V  `   V  \#\| |  | |/#/  V   '  V  V  \|  '
 *              `   `  `      `   / | |  | | \   '      '  '   '
 *                               (  | |  | |  )
 *                              __\ | |  | | /__
 *                             (vvv(VVV)(VVV)vvv)
 *
 *                              HERE BE DRAGONS
 *
 */

package com.github.shadowsocks.utils

import android.content.{Intent, SharedPreferences}

object Executable {
  val REDSOCKS = "redsocks"
  val PDNSD = "pdnsd"
  val SS_LOCAL = "ss-local"
  val SS_TUNNEL = "ss-tunnel"
  val IPTABLES = "iptables"
  val TUN2SOCKS = "tun2socks"
}

object Msg {
  val CONNECT_FINISH = 1
  val CONNECT_SUCCESS = 2
  val CONNECT_FAIL = 3
  val VPN_ERROR = 6
}

object Path {
  val BASE = "/data/data/com.github.shadowsocks/"
}

object Key {
  val profileId = "profileId"
  val profileName = "profileName"

  val proxied = "Proxyed"

  val isNAT = "isNAT"
  val isRoot = "isRoot"
  val status = "status"
  val proxyedApps = "proxyedApps"
  val route = "route"

  val isRunning = "isRunning"
  val isAutoConnect = "isAutoConnect"

  val isGlobalProxy = "isGlobalProxy"
  val isGFWList = "isGFWList"
  val isBypassApps = "isBypassApps"
  val isTrafficStat = "isTrafficStat"
  val isUdpDns = "isUdpDns"

  val proxy = "proxy"
  val sitekey = "sitekey"
  val encMethod = "encMethod"
  val remotePort = "remotePort"
  val localPort = "port"
}

object Scheme {
  val APP = "app://"
  val PROFILE = "profile://"
  val SS = "ss"
}

object Mode {
  val NAT = 0
  val VPN = 1
}

object State {
  val INIT = 0
  val CONNECTING = 1
  val CONNECTED = 2
  val STOPPING = 3
  val STOPPED = 4
  def isAvailable(state: Int): Boolean = state != CONNECTED && state != CONNECTING
}

object Action {
  val SERVICE = "com.github.shadowsocks.SERVICE"
  val CLOSE = "com.github.shadowsocks.CLOSE"
  val UPDATE_FRAGMENT = "com.github.shadowsocks.ACTION_UPDATE_FRAGMENT"
  val UPDATE_PREFS = "com.github.shadowsocks.ACTION_UPDATE_PREFS"
  val UPDATE_STATE = "com.biganiseed.shadowsocks.ACTION_UPDATE_STATE"
}

object Route {
  val ALL = "all"
  val BYPASS_LAN = "bypass-lan"
  val BYPASS_CHN = "bypass-china"
}

object Extra {
  val STATE = "state"
  val MESSAGE = "message"

  // def save(settings: SharedPreferences, config: Config) {
  //   val edit = settings.edit()

  //   edit.putBoolean(Key.isGlobalProxy, config.isGlobalProxy)
  //   edit.putBoolean(Key.isGFWList, config.isGFWList)
  //   edit.putBoolean(Key.isBypassApps, config.isBypassApps)
  //   edit.putBoolean(Key.isTrafficStat, config.isTrafficStat)

  //   edit.putString(Key.profileName, config.profileName)
  //   edit.putString(Key.proxy, config.proxy)
  //   edit.putString(Key.sitekey, config.sitekey)
  //   edit.putString(Key.encMethod, config.encMethod)
  //   edit.putString(Key.remotePort, config.remotePort.toString)
  //   edit.putString(Key.localPort, config.localPort.toString)

  //   edit.apply()
  // }

  // def get(intent: Intent): Config = {
  //   val isGlobalProxy = intent.getBooleanExtra(Key.isGlobalProxy, false)
  //   val isGFWList = intent.getBooleanExtra(Key.isGFWList, false)
  //   val isBypassApps = intent.getBooleanExtra(Key.isBypassApps, false)
  //   val isTrafficStat = intent.getBooleanExtra(Key.isTrafficStat, false)

  //   val profileName = intent.getStringExtra(Key.profileName)
  //   val proxy = intent.getStringExtra(Key.proxy)
  //   val sitekey = intent.getStringExtra(Key.sitekey)
  //   val encMethod = intent.getStringExtra(Key.encMethod)
  //   val remotePort = intent.getIntExtra(Key.remotePort, 1984)
  //   val localPort = intent.getIntExtra(Key.localPort, 1984)
  //   val proxiedString = intent.getStringExtra(Key.proxied)

  //   new Config(isGlobalProxy, isGFWList, isBypassApps, isTrafficStat, profileName, proxy, sitekey,
  //     encMethod, remotePort, localPort, proxiedString)
  // }

  // def put(settings: SharedPreferences, intent: Intent) {
  //   val isGlobalProxy = settings.getBoolean(Key.isGlobalProxy, false)
  //   val isGFWList = settings.getBoolean(Key.isGFWList, false)
  //   val isBypassApps = settings.getBoolean(Key.isBypassApps, false)
  //   val isTrafficStat = settings.getBoolean(Key.isTrafficStat, false)


  //   val profileName = settings.getString(Key.profileName, "default")
  //   val proxy = settings.getString(Key.proxy, "127.0.0.1")
  //   val sitekey = settings.getString(Key.sitekey, "default")
  //   val encMethod = settings.getString(Key.encMethod, "table")
  //   val remotePort: Int = try {
  //     settings.getString(Key.remotePort, "1984").toInt
  //   } catch {
  //     case ex: NumberFormatException => {
  //       1984
  //     }
  //   }
  //   val localProt: Int = try {
  //     settings.getString(Key.localPort, "1984").toInt
  //   } catch {
  //     case ex: NumberFormatException => {
  //       1984
  //     }
  //   }
  //   val proxiedAppString = settings.getString(Key.proxied, "")

  //   intent.putExtra(Key.isGlobalProxy, isGlobalProxy)
  //   intent.putExtra(Key.isGFWList, isGFWList)
  //   intent.putExtra(Key.isBypassApps, isBypassApps)
  //   intent.putExtra(Key.isTrafficStat, isTrafficStat)

  //   intent.putExtra(Key.profileName, profileName)
  //   intent.putExtra(Key.proxy, proxy)
  //   intent.putExtra(Key.sitekey, sitekey)
  //   intent.putExtra(Key.encMethod, encMethod)
  //   intent.putExtra(Key.remotePort, remotePort)
  //   intent.putExtra(Key.localPort, localProt)

  //   intent.putExtra(Key.proxied, proxiedAppString)
  // }
}
