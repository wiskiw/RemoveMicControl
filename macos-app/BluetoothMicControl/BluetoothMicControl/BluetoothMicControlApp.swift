//
//  BluetoothMicControlApp.swift
//  BluetoothMicControl
//
//  Created by Andrei Yablonski on 30.11.22.
//

import SwiftUI
import AudioToolbox
import AVKit
import SimplyCoreAudio

@main
struct BluetoothMicControlApp: App {
    
    @NSApplicationDelegateAdaptor(AppDelegate.self) private var appDelegate
    
    var body: some Scene {
        WindowGroup {
            EmptyView().frame(width: 0, height: 0)
        }
    }
    
    init() {
        //        switch AVCaptureDevice.authorizationStatus(for: .audio) {
        //            case .notDetermined:
        //                AVCaptureDevice.requestAccess(for: .audio) { granted in
        //                    if !granted {
        //                        NSLog("Can't get access to the mic.")
        //                        exit(1)
        //                    }
        //                }
        //
        //            case .denied:
        //                fallthrough
        //            case .restricted:
        //                NSLog("Can't get access to the mic.")
        //                exit(1)
        //            default:
        //                print("Already has permission");
        //        }
    }
}


class AppDelegate : NSObject, NSApplicationDelegate {

    private let inputDeviceService : InputDeviceService
    private let outputDeviceService : OutputDeviceService
    let micControlViewModel : MicControlViewModel
    
    private var statusBarItem: NSStatusItem!
    private var popover : NSPopover!
    
    override init() {
        let simplyCA = SimplyCoreAudio()
        self.inputDeviceService = InputDeviceService(simplyCA: simplyCA)
        self.outputDeviceService = try! OutputDeviceService(simplyCA: simplyCA)
        
        self.micControlViewModel = MicControlViewModel(
            inputDeviceService: self.inputDeviceService,
            outputDeviceService: self.outputDeviceService
        )
    }
    
    func applicationDidFinishLaunching(_ notification: Notification) {
        // Close main app window
        if let window = NSApplication.shared.windows.first {
            window.close()
        }
        
        // Create the status item in the Menu bar
        statusBarItem = NSStatusBar.system.statusItem(withLength: CGFloat(NSStatusItem.variableLength))
      
        // Status bar button
        if let iconButton = statusBarItem.button {
            iconButton.title = "BluetoothMicControl"
            iconButton.action = #selector(togglePopover)
            iconButton.image = getMicIcon(isMuted: self.inputDeviceService.isMuted())
        }

        self.popover = NSPopover()
        self.popover.contentSize = NSSize(width: 300, height: 300)
        self.popover.behavior = .transient
        self.popover.animates = true
        
        let popoverView = MicControlView(vm: self.micControlViewModel)
        self.popover.contentViewController = NSHostingController(
            rootView: popoverView.frame(maxWidth: .infinity, maxHeight: .infinity).padding()
        )

        inputDeviceService.muteListener = { isMuted in
            self.statusBarItem.button?.image = self.getMicIcon(isMuted: isMuted)
        }
    }
    
    @objc func togglePopover() {
        if (popover.isShown){
            self.popover.performClose(nil)
        } else {
            if let iconButton = statusBarItem.button {
                self.popover.show(relativeTo: iconButton.bounds, of: iconButton, preferredEdge: NSRectEdge.maxY)
                
                // https://stackoverflow.com/a/54483792
                NSApp.activate(ignoringOtherApps: true)
            }
        }
    }
    
    private func getMicIcon(isMuted : Bool) -> NSImage{
        if (isMuted){
            return NSImage(named: "ic_mic_off")!
        } else {
            return NSImage(named: "ic_mic_on")!
        }
    }
    
    private func getMicMutedText(isMuted : Bool) -> String{
        if (isMuted){
            return "MUTED"
        } else {
            return "ACTIVATED"
        }
    }
}
