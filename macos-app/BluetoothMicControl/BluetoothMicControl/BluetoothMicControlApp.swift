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
    
    static let popoverWidth = 296
    static let popoverHeigth = 230
    
    @NSApplicationDelegateAdaptor(AppDelegate.self) private var appDelegate
    
    var body: some Scene {
        WindowGroup {
            EmptyView().frame(width: 0, height: 0)
        }
    }
}


class AppDelegate : NSObject, NSApplicationDelegate, ObservableObject {

    private let inputDeviceService: InputDeviceService
    private let outputDeviceService: OutputDeviceService
    private let micControlViewModel : MicControlViewModel
    
    private var popover: NSPopover!
    private var statusBarController: StatusBarController!

    private var cancellables = Set<AnyCancellable>()
    
    override init() {
        let simplyCA = SimplyCoreAudio()

        self.inputDeviceService = InputDeviceService(simplyCA: simplyCA)
        self.outputDeviceService = try! OutputDeviceService(simplyCA: simplyCA)
        
        self.micControlViewModel = MicControlViewModel(inputDeviceService: self.inputDeviceService, outputDeviceService: self.outputDeviceService)
    }
    
    func applicationDidFinishLaunching(_ notification: Notification) {
        // Close main app window
        if let window = NSApplication.shared.windows.first {
            window.close()
        }
        
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
        popover.contentSize = NSSize(width: BluetoothMicControlApp.popoverWidth, height: BluetoothMicControlApp.popoverHeigth)
        popover.behavior = .transient
        popover.animates = false
        
        let popoverView = MicControlView(micControlViewModel: self.micControlViewModel)
        let hostingController = NSHostingController(rootView: popoverView.frame(maxWidth: .infinity, maxHeight: .infinity))
        popover.contentViewController = hostingController
        
        // Create the Status Bar Item with the above Popover
        statusBarController = StatusBarController(
            popover : popover,
            icon: self.micControlViewModel.micState.getMicStatusBarIcon()
        )
    }
    
    func applicationWillTerminate(_ notification: Notification) {
        inputDeviceService.activate()
        cancellables.removeAll()
    }
}
