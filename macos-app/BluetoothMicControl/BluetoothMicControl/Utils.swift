//
//  Utils.swift
//  BluetoothMicControl
//
//  Created by Andrei Yablonski on 2.12.22.
//

import Foundation

func sleepMs(durationMs : Int){
    usleep(useconds_t(durationMs) * 1000) // *1000 - millionths->milliseconds
}
