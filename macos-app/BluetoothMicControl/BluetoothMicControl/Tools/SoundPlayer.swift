//
//  SoundPlayer.swift
//  BluetoothMicControl
//
//  Created by Andrei Yablonski on 4.12.22.
//

import Foundation
import AVFoundation
import AppKit


// source: https://gist.github.com/marcosgriselli/1b573f5d011636838c8ba962f7749981
class SoundPlayer {
    
    private let audioPlayer: AVAudioPlayer
    
    init(filename: String, fileExtension: String) throws {
        guard let url = Bundle.main.url(forResource: filename, withExtension: fileExtension) else {
            throw AudioLoaderError.resourceNotFound
        }
        
        do {
            self.audioPlayer = try AVAudioPlayer(contentsOf: url)
        } catch {
            throw AudioLoaderError.loadPlayer
        }
    }
    
    init(filename: String) throws {
        guard let dataAsset = NSDataAsset(name: filename) else {
            throw AudioLoaderError.assetNotFound
        }

        do {
            self.audioPlayer = try AVAudioPlayer(data: dataAsset.data, fileTypeHint: AVFileType.wav.rawValue)
        } catch {
            throw AudioLoaderError.loadPlayer
        }
    }
    
    func play() {
        DispatchQueue.global(qos: .userInitiated).async {
            self.audioPlayer.play()
        }
    }

    enum AudioLoaderError: Error {
        case resourceNotFound
        case assetNotFound
        case loadPlayer
    }
}
