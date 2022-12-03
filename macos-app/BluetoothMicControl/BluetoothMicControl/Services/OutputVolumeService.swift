//
//  OutputVolumeService.swift
//  BluetoothMicControl
//
//  Created by Andrei Yablonski on 30.11.22.
//

import Foundation
import SimplyCoreAudio

class OutputVolumeService {

    private let simplyCA: SimplyCoreAudio!
    private let throttleQueue = OperationQueue()
    
    private lazy var lastVolume : Float32 = self.getVolume()
    private var volumeChangedObserver : NSObjectProtocol? = nil
    private var ignoreNext : Bool = false
    
    var onVolumeChangedListener: ((Float32, Float32)->(Bool))? = nil
    
    init (simplyCA : SimplyCoreAudio){
        self.simplyCA = simplyCA
        
        NSLog("OutputVolumeService init")
        NSLog("Init volume \(self.lastVolume)")
        
        self.volumeChangedObserver = NotificationCenter.default.addObserver(forName: .deviceVolumeDidChange,
                                                                            object: nil,
                                                                            queue: .main) { (notification) in
            self.handleVolumeChangedEvent()
        }
    }
    
    private func handleVolumeChangedEvent() {
        let throttleOperation = ThrottleOperation() {
            if (self.ignoreNext){
                self.ignoreNext = false
                return
            }
            
            let old = self.lastVolume
            let new = self.getVolume()
            
            let isRollbackRequired = self.onVolumeChangedListener?(old, new) ?? false
            
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
    
    func getVolume() -> Float32{
        guard let device = self.simplyCA.defaultOutputDevice else { return 0.0 }
        
        if let stereoPair = device.preferredChannelsForStereo(scope: .output) {
            let leftVolume = device.volume(channel: stereoPair.left, scope: .output) ?? 0.0
            let rightVolume = device.volume(channel: stereoPair.right, scope: .output) ?? 0.0
            
            return (leftVolume + rightVolume) / 2.0
        }
        return 0.0
    }
    
    func setVolume(volume : Float32) {
        guard let device = self.simplyCA.defaultOutputDevice else { return }
        
        if let stereoPair = device.preferredChannelsForStereo(scope: .output) {
            device.setVolume(volume, channel: stereoPair.left, scope: .output)
            device.setVolume(volume, channel: stereoPair.right, scope: .output)
        }
    }
    
    deinit {
        if let observer = volumeChangedObserver {
            NotificationCenter.default.removeObserver(observer)
        }
    }
    
    
    class ThrottleOperation: Operation {
        
        private let THROTTLE_DELAY_MS = 96
        private let onComplete : (() -> Void)!
        
        init(onComplete : @escaping () -> Void) {
            self.onComplete = onComplete
        }
        
        override func main() {
            //            NSLog("Trottling...")
            sleepMs(durationMs: self.THROTTLE_DELAY_MS)
            
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
