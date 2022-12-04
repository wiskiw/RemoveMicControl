//
//  StatusBarController.swift
//  BluetoothMicControl
//
//  Created by Andrei Yablonski on 4.12.22.
//

import Foundation
import AppKit

class StatusBarController {

    private let popover: NSPopover
    private let statusBar: NSStatusBar
    private let statusBarItem: NSStatusItem
    private var hidePopoverEventMonitor : EventMonitor?
    
    init(popover: NSPopover, icon : NSImage) {
        self.popover = popover
        self.statusBar = NSStatusBar.init()

        // Create the status item in the Menu bar
        self.statusBarItem = NSStatusBar.system.statusItem(withLength: CGFloat(NSStatusItem.variableLength))
        self.setIcon(icon: icon)
        
        if let statusBarButton = statusBarItem.button {
            statusBarButton.action = #selector(togglePopover)
            statusBarButton.target = self
        }
        
        
        self.hidePopoverEventMonitor = EventMonitor(
            mask: [.leftMouseDown, .rightMouseDown],
            handler: { event in
                self.hidePopover()
            }
        )
    }
    
    @objc func togglePopover() {
        if(popover.isShown) {
            hidePopover()
        } else {
            showPopover()
        }
    }

    func showPopover() {
        hidePopoverEventMonitor?.start()

        if let statusBarButton = statusBarItem.button {
            popover.show(relativeTo: statusBarButton.bounds, of: statusBarButton, preferredEdge: NSRectEdge.maxY)
        }
    }
    
    func hidePopover() {
        hidePopoverEventMonitor?.stop()
        popover.performClose(nil)
    }
    
    func setIcon(icon: NSImage){
        statusBarItem.button?.image = icon
    }
}
