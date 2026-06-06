package com.hbm.ntm.client.obj;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public final class LegacyObjScanCli {
    public static void main(String[] args) throws IOException {
        if (args.length < 1 || args.length > 3) {
            System.err.println("Usage: LegacyObjScanCli <modelRoot> [markdownOut] [csvOut]");
            System.exit(2);
        }

        LegacyObjScan.ScanReport report = LegacyObjScan.scanDirectory(Path.of(args[0]));
        if (args.length >= 2) {
            report.writeMarkdown(Path.of(args[1]));
        }
        if (args.length >= 3) {
            report.writeCsv(Path.of(args[2]));
        }

        System.out.println("models=" + report.models().size());
        for (Map.Entry<LegacyObjScan.Compatibility, Integer> entry : report.countsByCompatibility().entrySet()) {
            System.out.println(entry.getKey() + "=" + entry.getValue());
        }
    }

    private LegacyObjScanCli() {
    }
}
