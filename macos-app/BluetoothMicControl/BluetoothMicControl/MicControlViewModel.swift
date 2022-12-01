//
//  MicControlViewModel.swift
//  BluetoothMicControl
//
//  Created by Andrei Yablonski on 30.11.22.
//

import Foundation

class MicControlViewModel : ObservableObject {
    
    private let volumeService = VolumeService()
    
    @Published var debugMessage : String = "nothing"
    
    init() {
        volumeService.onVolumeChangedListener = { old, new in
            print("volume: \(old) -> \(new)")
            self.volumeService.setVolume(volume: old)
        }
    }
    
    func populateUi(){
        self.debugMessage = "Hello there!"
    }

}
