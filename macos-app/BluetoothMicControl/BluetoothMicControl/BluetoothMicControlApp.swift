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
import Combine

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


class AppDelegate : NSObject, NSApplicationDelegate, ObservableObject {

    let micControlViewModel : MicControlViewModel
    
    private var popover: NSPopover!
    private var statusBarController: StatusBarController!

    private var cancellables = Set<AnyCancellable>()
    
    override init() {
        let simplyCA = SimplyCoreAudio()

        let inputDeviceService = InputDeviceService(simplyCA: simplyCA)
        let outputDeviceService = try! OutputDeviceService(simplyCA: simplyCA)
        
        self.micControlViewModel = MicControlViewModel(
            inputDeviceService: inputDeviceService,
            outputDeviceService: outputDeviceService
        )
    }
    
    func applicationDidFinishLaunching(_ notification: Notification) {
        // Close main app window
//        if let window = NSApplication.shared.windows.first {
//            window.close()
//        }
        
        setupStatusBar()

        self.micControlViewModel.$micState
            .sink { micState in
                self.statusBarController?.setIcon(icon: micState.getMicStatusBarIcon())
            }
            .store(in: &cancellables)
    }
    
    private func setupStatusBar() {
        // Set the SwiftUI's ContentView to the Popover's ContentViewController
        popover = NSPopover()
        popover.contentSize = NSSize(width: 300, height: 220)
        popover.behavior = .transient
        popover.animates = false
        
        let popoverView = MicControlView(vm: self.micControlViewModel)
        let hostingController = NSHostingController(rootView: popoverView.frame(maxWidth: .infinity, maxHeight: .infinity))
        popover.contentViewController = hostingController
        
        // Create the Status Bar Item with the above Popover
        statusBarController = StatusBarController(
            popover : popover,
            icon: self.micControlViewModel.micState.getMicStatusBarIcon()
        )
    }
    
    func applicationWillTerminate(_ notification: Notification) {
        cancellables.removeAll()
    }
}
