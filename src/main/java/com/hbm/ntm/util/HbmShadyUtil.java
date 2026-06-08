package com.hbm.ntm.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public final class HbmShadyUtil {
    public static final String HB_MINECRAFT = "192af5d7-ed0f-48d8-bd89-9d41af8524f8";
    public static final String LPKUKIN = "937c9804-e11f-4ad2-a5b1-42e62ac73077";
    public static final String DAFNIK = "3af1c262-61c0-4b12-a4cb-424cc3a9c8c0";
    public static final String A20 = "4729b498-a81c-42fd-8acd-20d6d9f759e0";
    public static final String LORD_VERTICE = "a41df45e-13d8-4677-9398-090d3882b74f";
    public static final String CODE_RED = "912ec334-e920-4dd7-8338-4d9b2d42e0a1";
    public static final String DXMASTER769 = "62c168b2-d11d-4dbf-9168-c6cea3dcb20e";
    public static final String DR_NOSTALGIA = "e82684a7-30f1-44d2-ab37-41b342be1bbd";
    public static final String SAMINO2 = "87c3960a-4332-46a0-a929-ef2a488d1cda";
    public static final String HOBOY03NEW = "d7f29d9c-5103-4f6f-88e1-2632ff95973f";
    public static final String DRAGON59MC = "dc23a304-0f84-4e2d-b47d-84c8d3bfbcdb";
    public static final String STEELCOURAGE = "ac49720b-4a9a-4459-a26f-bee92160287a";
    public static final String ZIPPY_SQRL = "03c20435-a229-489a-a1a1-671b803f7017";
    public static final String SCHRABBY = "3a4a1944-5154-4e67-b80a-b6561e8630b7";
    public static final String SWEATY_SWIGGS = "5544aa30-b305-4362-b2c1-67349bb499d5";
    public static final String DRILLGON = "41ebd03f-7a12-42f3-b037-0caa4d6f235b";
    public static final String DOCTOR17 = "e4ab1199-1c22-4f82-a516-c3238bc2d0d1";
    public static final String DOCTOR17PH = "4d0477d7-58da-41a9-a945-e93df8601c5a";
    public static final String SHIMMERING_BLAZE = "061bc566-ec74-4307-9614-ac3a70d2ef38";
    public static final String FIFE_MINER = "37e5eb63-b9a2-4735-9007-1c77d703daa3";
    public static final String LAG_ADD = "259785a0-20e9-4c63-9286-ac2f93ff528f";
    public static final String PU_238 = "c95fdfd3-bea7-4255-a44b-d21bc3df95e3";
    public static final String TANKISH = "609268ad-5b34-49c2-abba-a9d83229af03";
    public static final String FRIZZLE_FRAZZLE = "fc4cc2ee-12e8-4097-b26a-1c6cb1b96531";
    public static final String THE_NCR = "28ae585f-4431-4491-9ce8-3def6126e3c6";
    public static final String BARNABY99_X = "b04cf173-cff0-4acd-aa19-3d835224b43d";
    public static final String MA118 = "1121cb7a-8773-491f-8e2b-221290c93d81";
    public static final String ADAM29ADAM29 = "bbae7bfa-0eba-40ac-a0dd-f3b715e73e61";
    public static final String ALCATER = "0b399a4a-8545-45a1-be3d-ece70d7d48e9";
    public static final String EGE444 = "42ee978c-442a-4cd8-95b6-29e469b6df10";
    public static final String LE_PEEPER_SAUVAGE = "433c2bb7-018c-4d51-acfe-27f907432b5e";

    public static final Set<String> HASHES;
    public static final Set<String> CONTRIBUTORS;
    public static final Map<String, String> LEGACY_UUIDS;

    static {
        Set<String> hashes = new HashSet<>();
        hashes.add("41de5c372b0589bbdb80571e87efa95ea9e34b0d74c6005b8eab495b7afd9994");
        hashes.add("31da6223a100ed348ceb3254ceab67c9cc102cb2a04ac24de0df3ef3479b1036");
        HASHES = Collections.unmodifiableSet(hashes);

        Set<String> contributors = new HashSet<>();
        contributors.add("06ab7c03-55ce-43f8-9d3c-2850e3c652de");
        contributors.add("5bf069bc-5b46-4179-aafe-35c0a07dee8b");
        contributors.add("ccd9aa1c-26b9-4dde-8f37-b96f8d99de22");
        CONTRIBUTORS = Collections.unmodifiableSet(contributors);

        Map<String, String> uuids = new LinkedHashMap<>();
        uuids.put("HbMinecraft", HB_MINECRAFT);
        uuids.put("LPkukin", LPKUKIN);
        uuids.put("Dafnik", DAFNIK);
        uuids.put("a20", A20);
        uuids.put("LordVertice", LORD_VERTICE);
        uuids.put("CodeRed_", CODE_RED);
        uuids.put("dxmaster769", DXMASTER769);
        uuids.put("Dr_Nostalgia", DR_NOSTALGIA);
        uuids.put("Samino2", SAMINO2);
        uuids.put("Hoboy03new", HOBOY03NEW);
        uuids.put("Dragon59MC", DRAGON59MC);
        uuids.put("Steelcourage", STEELCOURAGE);
        uuids.put("ZippySqrl", ZIPPY_SQRL);
        uuids.put("Schrabby", SCHRABBY);
        uuids.put("SweatySwiggs", SWEATY_SWIGGS);
        uuids.put("Drillgon", DRILLGON);
        uuids.put("Doctor17", DOCTOR17);
        uuids.put("Doctor17PH", DOCTOR17PH);
        uuids.put("ShimmeringBlaze", SHIMMERING_BLAZE);
        uuids.put("FifeMiner", FIFE_MINER);
        uuids.put("lag_add", LAG_ADD);
        uuids.put("Pu_238", PU_238);
        uuids.put("Tankish", TANKISH);
        uuids.put("FrizzleFrazzle", FRIZZLE_FRAZZLE);
        uuids.put("the_NCR", THE_NCR);
        uuids.put("Barnaby99_x", BARNABY99_X);
        uuids.put("Ma118", MA118);
        uuids.put("Adam29Adam29", ADAM29ADAM29);
        uuids.put("Alcater", ALCATER);
        uuids.put("ege444", EGE444);
        uuids.put("LePeeperSauvage", LE_PEEPER_SAUVAGE);
        LEGACY_UUIDS = Collections.unmodifiableMap(uuids);
    }

    private HbmShadyUtil() {
    }

    public static String encode(String message) {
        return Base64.getEncoder().encodeToString(safe(message).getBytes(StandardCharsets.UTF_8));
    }

    public static String decode(String message) {
        return new String(Base64.getDecoder().decode(safe(message)), StandardCharsets.UTF_8);
    }

    public static String smoosh(String s1, String s2, String s3, String s4) {
        byte[] b1 = safe(s1).getBytes(StandardCharsets.UTF_8);
        byte[] b2 = safe(s2).getBytes(StandardCharsets.UTF_8);
        byte[] b3 = safe(s3).getBytes(StandardCharsets.UTF_8);
        byte[] b4 = safe(s4).getBytes(StandardCharsets.UTF_8);
        if (b1.length == 0 || b2.length == 0 || b3.length == 0 || b4.length == 0) {
            return "";
        }

        Random random = new Random();
        StringBuilder builder = new StringBuilder();
        builder.append(s1);
        random.setSeed(b1[0]);
        builder.append(random.nextInt(0xffffff));
        builder.append(s2);
        random.setSeed(random.nextInt(0xffffff) + b2[0]);
        random.setSeed(b2[0]);
        builder.append(random.nextInt(0xffffff));
        builder.append(s3);
        random.setSeed(random.nextInt(0xffffff) + b3[0]);
        random.setSeed(b3[0]);
        builder.append(random.nextInt(0xffffff));
        builder.append(s4);
        random.setSeed(random.nextInt(0xffffff) + b4[0]);
        random.setSeed(b4[0]);
        builder.append(random.nextInt(0xffffff));
        return getHash(builder.toString());
    }

    public static String getHash(String input) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] bytes = sha256.digest(safe(input).getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(bytes.length * 2);
            for (byte value : bytes) {
                builder.append(Integer.toString((value & 0xFF) + 256, 16).substring(1));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException exception) {
            return "";
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
