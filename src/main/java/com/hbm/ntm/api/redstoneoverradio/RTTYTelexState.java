package com.hbm.ntm.api.redstoneoverradio;

import java.util.Arrays;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import com.hbm.ntm.util.HbmItemStackUtil;

public class RTTYTelexState {
    public static final int LINE_COUNT = 5;
    public static final int LINE_WIDTH = 33;
    public static final char EOL = '\n';
    public static final char EOT = '\u0004';
    public static final char BELL = '\u0007';
    public static final char PRINT = '\u000c';
    public static final char PAUSE = '\u0016';
    public static final char CLEAR = '\u007f';

    private final String[] txBuffer = new String[] {"", "", "", "", ""};
    private final String[] rxBuffer = new String[] {"", "", "", "", ""};
    private String txChannel = "";
    private String rxChannel = "";
    private int sendingLine;
    private int sendingIndex;
    private boolean sending;
    private int sendingWait;
    private int writingLine;
    private boolean printAfterRx;
    private boolean deleteOnReceive = true;
    private char sendingChar = ' ';

    public void tick(Level level, BlockPos pos) {
        if (level == null || level.isClientSide) {
            return;
        }
        sendingChar = ' ';
        tickSending(level);
        tickReceiving(level, pos);
    }

    public boolean applyControl(CompoundTag tag, Level level, BlockPos pos) {
        boolean changed = false;
        for (int i = 0; i < LINE_COUNT; i++) {
            String key = "tx" + i;
            if (tag.contains(key, Tag.TAG_STRING)) {
                txBuffer[i] = truncate(tag.getString(key));
                changed = true;
            }
        }

        String command = tag.getString("cmd");
        if ("snd".equals(command) && !sending) {
            sending = true;
            sendingLine = 0;
            sendingIndex = 0;
            changed = true;
        } else if ("rxprt".equals(command)) {
            print(level, pos);
        } else if ("rxcls".equals(command)) {
            clearReceive();
            changed = true;
        } else if ("sve".equals(command)) {
            txChannel = clean(tag.getString("txChan"));
            rxChannel = clean(tag.getString("rxChan"));
            changed = true;
        }
        return changed;
    }

    public void save(CompoundTag tag) {
        for (int i = 0; i < LINE_COUNT; i++) {
            tag.putString("tx" + i, txBuffer[i]);
            tag.putString("rx" + i, rxBuffer[i]);
        }
        tag.putString("txChan", txChannel);
        tag.putString("rxChan", rxChannel);
    }

    public void load(CompoundTag tag) {
        for (int i = 0; i < LINE_COUNT; i++) {
            txBuffer[i] = truncate(tag.getString("tx" + i));
            rxBuffer[i] = clean(tag.getString("rx" + i));
        }
        txChannel = clean(tag.getString("txChan"));
        rxChannel = clean(tag.getString("rxChan"));
        resetTransientState();
    }

    public void saveClient(CompoundTag tag) {
        for (int i = 0; i < LINE_COUNT; i++) {
            tag.putString("tx" + i, txBuffer[i]);
            tag.putString("rx" + i, rxBuffer[i]);
        }
        tag.putString("txChan", txChannel);
        tag.putString("rxChan", rxChannel);
        tag.putInt("sendingChar", sendingChar);
        tag.putBoolean("isSending", sending);
    }

    public void loadClient(CompoundTag tag) {
        for (int i = 0; i < LINE_COUNT; i++) {
            txBuffer[i] = truncate(tag.getString("tx" + i));
            rxBuffer[i] = clean(tag.getString("rx" + i));
        }
        txChannel = clean(tag.getString("txChan"));
        rxChannel = clean(tag.getString("rxChan"));
        sendingChar = (char) tag.getInt("sendingChar");
        sending = tag.getBoolean("isSending");
    }

    public String txChannel() {
        return txChannel;
    }

    public String rxChannel() {
        return rxChannel;
    }

    public char sendingChar() {
        return sendingChar;
    }

    public boolean isSending() {
        return sending;
    }

    public String txLine(int index) {
        return txBuffer[clampLine(index)];
    }

    public String rxLine(int index) {
        return rxBuffer[clampLine(index)];
    }

    public String[] txCopy() {
        return Arrays.copyOf(txBuffer, txBuffer.length);
    }

    public String[] rxCopy() {
        return Arrays.copyOf(rxBuffer, rxBuffer.length);
    }

    private void tickSending(Level level) {
        if (sending && txChannel.isEmpty()) {
            sending = false;
        }
        if (!sending) {
            return;
        }
        if (sendingWait > 0) {
            sendingWait--;
            return;
        }

        String line = txBuffer[sendingLine];
        if (line.length() > sendingIndex) {
            char c = line.charAt(sendingIndex++);
            if (c == PAUSE) {
                sendingWait = 20;
            } else {
                RTTYSystem.broadcast(level, txChannel, c);
                sendingChar = c;
            }
            return;
        }

        if (sendingLine >= LINE_COUNT - 1) {
            sending = false;
            RTTYSystem.broadcast(level, txChannel, EOT);
            sendingLine = 0;
            sendingIndex = 0;
        } else {
            RTTYSystem.broadcast(level, txChannel, EOL);
            sendingLine++;
            sendingIndex = 0;
        }
    }

    private void tickReceiving(Level level, BlockPos pos) {
        if (rxChannel.isEmpty()) {
            return;
        }
        RTTYSystem.RTTYChannel channel = RTTYSystem.listen(level, rxChannel);
        if (channel == null || !(channel.signal() instanceof Character c)
                || channel.timeStamp() <= level.getGameTime() - 2L || channel.timeStamp() == -1L) {
            return;
        }

        if (deleteOnReceive) {
            deleteOnReceive = false;
            clearReceive();
        }

        if (c == EOT) {
            if (printAfterRx) {
                printAfterRx = false;
                print(level, pos);
            }
            deleteOnReceive = true;
        } else if (c == EOL) {
            if (writingLine < LINE_COUNT - 1) {
                writingLine++;
            }
        } else if (c == BELL) {
            level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 2.0F, 0.5F);
        } else if (c == PRINT) {
            printAfterRx = true;
        } else if (c == CLEAR) {
            clearReceive();
        } else {
            rxBuffer[writingLine] += c;
        }
    }

    private void print(Level level, BlockPos pos) {
        if (level == null || level.isClientSide) {
            return;
        }
        String[] lines = Arrays.stream(rxBuffer)
                .filter(line -> line != null && !line.isEmpty())
                .toArray(String[]::new);
        ItemStack stack = HbmItemStackUtil.addTooltipToStack(new ItemStack(Items.PAPER), lines);
        stack.setHoverName(net.minecraft.network.chat.Component.literal("Message"));
        HbmItemStackUtil.dropStack(level, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, stack);
    }

    private void clearReceive() {
        Arrays.fill(rxBuffer, "");
        writingLine = 0;
    }

    private void resetTransientState() {
        sendingLine = 0;
        sendingIndex = 0;
        sending = false;
        sendingWait = 0;
        writingLine = 0;
        printAfterRx = false;
        deleteOnReceive = true;
        sendingChar = ' ';
    }

    private static String clean(String value) {
        return value == null ? "" : value;
    }

    private static String truncate(String value) {
        String cleaned = clean(value);
        return cleaned.length() <= LINE_WIDTH ? cleaned : cleaned.substring(0, LINE_WIDTH);
    }

    private static int clampLine(int index) {
        return Math.max(0, Math.min(LINE_COUNT - 1, index));
    }
}
