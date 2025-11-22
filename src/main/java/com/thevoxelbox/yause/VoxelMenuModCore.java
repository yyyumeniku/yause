package com.thevoxelbox.yause;

public class VoxelMenuModCore {
    
    private static VoxelMenuModCore instance;
    // main-menu panorama/renderer support removed — keep core minimal
    
    public VoxelMenuModCore() {
        instance = this;
    }
    
    public static VoxelMenuModCore getInstance() {
        if (instance == null) {
            instance = new VoxelMenuModCore();
        }
        return instance;
    }
    
    public String getVersion() {
        return "1.0.0";
    }
    
    public String getName() {
        return "Yause";
    }
    
    public void init() {
        // Initialization logic
        ForgeHandler.init("1.12.2");
    }
    
    // Panorama replacement and main-menu renderer removed in this port.
}
