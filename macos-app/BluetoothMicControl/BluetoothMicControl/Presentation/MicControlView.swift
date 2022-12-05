//
//  ContentView.swift
//  BluetoothMicControl
//
//  Created by Andrei Yablonski on 30.11.22.
//

import SwiftUI
import SimplyCoreAudio
import Combine

struct MicControlView: View {
    
    @StateObject private var micControlViewModel : MicControlViewModel
    
    init(micControlViewModel : MicControlViewModel){
        self._micControlViewModel = StateObject(wrappedValue: micControlViewModel)
    }
    

    var body: some View {
        ZStack(alignment: .leading) {
            VStack(alignment: .leading, spacing: 8){

                MicToggleButtonView()
                    .frame(maxWidth: .infinity)
                    .environmentObject(micControlViewModel)

                Spacer()
                
                if (!$micControlViewModel.isVolumeControlEnable.wrappedValue){
                    Text("Toggle the switch to enable volume control")
                        .font(.title2)
                        .lineLimit(nil)
                } else {
                    let isMicActive = micControlViewModel.micState == .activated
                    if (isMicActive){
                        Text("Turn down the volume to mute the mic")
                            .font(.title2)
                            .lineLimit(nil)
                    } else {
                        Text("Turn up the volume to activate the mic")
                            .font(.title2)
                            .lineLimit(nil)
                    }
                }
                
                Toggle("Enable Volume Control", isOn: $micControlViewModel.isVolumeControlEnable)
                    .toggleStyle(.switch)
                    .tint(Color("clr_mic_active"))
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .padding(16)
            

            // To close any other statusbar windows if this app was opened
            // idk why it's works :/
            let emptyList : [Int] = []
            List(emptyList, id: \.self) { _ in}.fixedSize()
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

struct MicToggleButtonView: View {

    @EnvironmentObject var micControlViewModel : MicControlViewModel
    
    var body: some View {
        let isMicActive = micControlViewModel.micState == .activated
        
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
        .frame(
            minWidth: .zero,
            maxWidth: .infinity,
            alignment: .leading
        )
        .background(
            ZStack {
                RoundedRectangle(cornerRadius: 10)
                    .stroke(isMicActive ? Color("clr_mic_active_dark") : Color("clr_mic_muted_dark"), lineWidth: 8)

                RoundedRectangle(cornerRadius: 10)
                    .fill(isMicActive ? Color("clr_mic_active") : Color("clr_mic_muted"))
            }
           
        )
        .onTapGesture {
            micControlViewModel.toggleMicState()
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    
    private static let simplyCA = SimplyCoreAudio()
    private static let inputDeviceService = InputDeviceService(simplyCA: simplyCA)
    private static let outputDeviceService = try! OutputDeviceService(simplyCA: simplyCA)
    
    private static let micControlViewModel = MicControlViewModel(inputDeviceService: inputDeviceService, outputDeviceService: outputDeviceService)
    
    static var previews: some View {
        MicControlView(
            micControlViewModel: micControlViewModel
        )
        .frame(
            width: CGFloat(BluetoothMicControlApp.popoverWidth),
            height: CGFloat(BluetoothMicControlApp.popoverHeigth)
        )
    }
}
