package file_moving;

import eu.ibagroup.easyrpa.engine.boot.ApModuleRunner;

public class LocalRunner {
    public static void main(String[] args) {
        ApModuleRunner.localLaunch(FileMovingModule.class);
    }
}
