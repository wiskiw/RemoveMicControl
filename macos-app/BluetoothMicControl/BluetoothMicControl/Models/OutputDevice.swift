//
//  OutputDevice.swift
//  BluetoothMicControl
//
//  Created by Andrei Yablonski on 3.12.22.
//

import Foundation
import SimplyCoreAudio

class OutputDevice : Equatable {
    
    private let throttleQueue = OperationQueue()
    let audioDevice: AudioDevice
    
    private var lastVolume : Float32 = 0.0
    private var volumeChangedObserver : NSObjectProtocol? = nil
    private var ignoreNext : Bool = false
    
    var volumeChangedListener: ((Float32, Float32)->(Bool))? = nil
    
    init (audioDevice: AudioDevice){
        self.audioDevice = audioDevice
        self.throttleQueue.maxConcurrentOperationCount = 1
        
        self.lastVolume = self.getVolume()

        //        NSLog("Init OutputDevice '\(audioDevice.name)', volume:\(self.lastVolume)")
        
        self.volumeChangedObserver = NotificationCenter.default.addObserver(forName: .deviceVolumeDidChange,
                                                                            object: nil,
                                                                            queue: .main) { (notification) in
            self.handleVolumeChangedEvent()
        }
    }
    
    deinit {
        if let observer = volumeChangedObserver {
            NotificationCenter.default.removeObserver(observer)
        }
    }
    
    private func handleVolumeChangedEvent() {
        let throttleOperation = VolumeChangedThrottleOperation() {
            if (self.ignoreNext){
                self.ignoreNext = false
                return
            }
            
            let old = self.lastVolume
            let new = self.getVolume()
            
            if (old == new || self.volumeChangedListener == nil) {
                return
            }
            
            let isRollbackRequired = self.volumeChangedListener?(old, new) ?? false
            
            if (isRollbackRequired) {
                self.ignoreNext = true
                self.setVolume(volume: old)
            } else {
                self.lastVolume = new
            }
        }
        
        self.throttleQueue.cancelAllOperations()
        self.throttleQueue.addOperation(throttleOperation)
    }
    
    func getVolume() -> Float32 {
        return self.audioDevice.virtualMainVolume(scope: .output) ?? 0.0
    }
    
    func setVolume(volume : Float32) {
        self.audioDevice.setVirtualMainVolume(volume, scope: .output)
    }
    
    static func == (lhs: OutputDevice, rhs: OutputDevice) -> Bool {
        return lhs.audioDevice.id == rhs.audioDevice.id
    }
    
    
    class VolumeChangedThrottleOperation: Operation {
        
        private static let throttleDelayMs = 96
        private let onComplete : (() -> Void)!
        
        init(onComplete : @escaping () -> Void) {
            self.onComplete = onComplete
        }
        
        override func main() {
//            NSLog("Trottling...")
            sleepMs(durationMs: VolumeChangedThrottleOperation.throttleDelayMs)
            
            if (isCancelled){
                return
            } else {
                DispatchQueue.main.async(){
//                    NSLog("Notifing...")
                    self.onComplete()
                }
            }
        }
    }
}
