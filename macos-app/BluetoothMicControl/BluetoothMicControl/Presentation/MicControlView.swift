//
//  ContentView.swift
//  BluetoothMicControl
//
//  Created by Andrei Yablonski on 30.11.22.
//

import SwiftUI

struct MicControlView: View {
    
    private static let MIC_ACTIVE_COLOR = ""
    private static let MIC_ACTIVE_DARK_COLOR = ""
    
    @StateObject private var vm : MicControlViewModel
    
    init(vm : MicControlViewModel){
        self._vm = StateObject(wrappedValue: vm)
    }
    
    
    var body: some View {
        let isMicActive = vm.micState == .activated
        
        ZStack(alignment: .leading) {
            VStack(alignment: .leading, spacing: 4) {
                Text("Mic")
                    .font(.system(size: 22, weight: .semibold))
                    .lineLimit(1)
                
                if (isMicActive) {
                    Text("Mic is ACTIVE")
                } else {
                    Text("Mic is MUTED")
                }
            }
            .padding(8)
            .frame(width: 156, alignment: .leading)
            .background(
                ZStack {
                    RoundedRectangle(cornerRadius: 10)
                        .stroke(isMicActive ? Color("clr_mic_active_dark") : Color("clr_mic_muted_dark"), lineWidth: 4)

                    RoundedRectangle(cornerRadius: 10)
                        .fill(isMicActive ? Color("clr_mic_active") : Color("clr_mic_muted_dark"))
                }
               
            )
            .onTapGesture {
                vm.toggleMicState()
            }
                
            
            // To close any other statusbar windows if this app was opened
            // idk why it's works :/
            let emptyList : [Int] = []
            List(emptyList, id: \.self) { _ in}.fixedSize()
        }
    }
    
}
