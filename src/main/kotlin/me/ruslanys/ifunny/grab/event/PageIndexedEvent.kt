package me.ruslanys.ifunny.grab.event

import me.ruslanys.ifunny.channel.Channel
import me.ruslanys.ifunny.channel.MemeInfo

class PageIndexedEvent(channel: Channel, val pageNumber: Int, val memesInfo: List<MemeInfo>) : GrabEvent(channel)
