package com.hbm.ntm.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public final class RedWireCoatedCt {
    public static final Direction[] DIRECTIONS = Direction.values();
    private static final int DEFAULT_FACE = packCtFace(0, 1, 2, 3);
    public static final Data DEFAULT_DATA = new Data((1 << DIRECTIONS.length) - 1,
            DEFAULT_FACE, DEFAULT_FACE, DEFAULT_FACE, DEFAULT_FACE, DEFAULT_FACE, DEFAULT_FACE);
    private static final int[][][] CT_ACCESS_BY_FACE = buildCtAccessByFace();

    private RedWireCoatedCt() {
    }

    public static Data compute(BlockGetter level, BlockPos pos, BlockState state) {
        if (level == null) {
            return DEFAULT_DATA;
        }
        int visibleMask = 0;
        int[] faces = new int[DIRECTIONS.length];
        for (Direction face : DIRECTIONS) {
            if (!isSameWire(level.getBlockState(pos.relative(face)), state)) {
                visibleMask |= 1 << face.ordinal();
            }
            faces[face.ordinal()] = computeFace(level, pos, state, face);
        }
        return new Data(visibleMask,
                faces[Direction.DOWN.ordinal()],
                faces[Direction.UP.ordinal()],
                faces[Direction.NORTH.ordinal()],
                faces[Direction.SOUTH.ordinal()],
                faces[Direction.WEST.ordinal()],
                faces[Direction.EAST.ordinal()]);
    }

    public static int computeFace(BlockGetter level, BlockPos pos, BlockState state, Direction face) {
        if (level == null) {
            return DEFAULT_FACE;
        }
        int connections = 0;
        int[][] dirs = CT_ACCESS_BY_FACE[face.ordinal()];
        for (int i = 0; i < dirs.length; i++) {
            int[] offset = dirs[i];
            if (isSameWire(level.getBlockState(pos.offset(offset[0], offset[1], offset[2])), state)) {
                connections |= 1 << i;
            }
        }
        int tl = cornerType(connected(connections, 3), connected(connections, 0), connected(connections, 1));
        int tr = 1 | cornerType(connected(connections, 4), connected(connections, 2), connected(connections, 1));
        int bl = 2 | cornerType(connected(connections, 3), connected(connections, 5), connected(connections, 6));
        int br = 3 | cornerType(connected(connections, 4), connected(connections, 7), connected(connections, 6));
        return packCtFace(tl, tr, bl, br);
    }

    public static int ctFaceFragment(int ctFace, int index) {
        int fragment = (ctFace >> (index * 5)) & 31;
        return Math.max(0, Math.min(19, fragment));
    }

    private static boolean isSameWire(BlockState other, BlockState state) {
        return other.is(state.getBlock());
    }

    private static int[][][] buildCtAccessByFace() {
        int[][][] access = new int[DIRECTIONS.length][][];
        for (Direction face : DIRECTIONS) {
            access[face.ordinal()] = ctAccess(face);
        }
        return access;
    }

    private static int[][] ctAccess(Direction face) {
        return switch (face) {
            case DOWN -> lexicalCoordinates(Direction.SOUTH, Direction.WEST);
            case UP -> lexicalCoordinates(Direction.NORTH, Direction.WEST);
            case NORTH -> lexicalCoordinates(Direction.UP, Direction.EAST);
            case SOUTH -> lexicalCoordinates(Direction.UP, Direction.WEST);
            case WEST -> lexicalCoordinates(Direction.UP, Direction.NORTH);
            case EAST -> lexicalCoordinates(Direction.UP, Direction.SOUTH);
        };
    }

    private static int[][] lexicalCoordinates(Direction up, Direction left) {
        Direction down = up.getOpposite();
        Direction right = left.getOpposite();
        return new int[][] {
                coordinatesFromSides(up, left),
                coordinatesFromSides(up),
                coordinatesFromSides(up, right),
                coordinatesFromSides(left),
                coordinatesFromSides(right),
                coordinatesFromSides(down, left),
                coordinatesFromSides(down),
                coordinatesFromSides(down, right)
        };
    }

    private static int[] coordinatesFromSides(Direction... directions) {
        int x = 0;
        int y = 0;
        int z = 0;
        for (Direction direction : directions) {
            x += direction.getStepX();
            y += direction.getStepY();
            z += direction.getStepZ();
        }
        return new int[] { x, y, z };
    }

    private static int cornerType(boolean horizontal, boolean corner, boolean vertical) {
        if (vertical && horizontal && corner) {
            return 4;
        } else if (vertical && horizontal) {
            return 8;
        } else if (vertical) {
            return 16;
        } else if (horizontal) {
            return 12;
        }
        return 0;
    }

    private static boolean connected(int connections, int index) {
        return (connections & (1 << index)) != 0;
    }

    private static int packCtFace(int topLeft, int topRight, int bottomLeft, int bottomRight) {
        return (topLeft & 31)
                | ((topRight & 31) << 5)
                | ((bottomLeft & 31) << 10)
                | ((bottomRight & 31) << 15);
    }

    public record Data(int visibleMask, int down, int up, int north, int south, int west, int east) {
        public boolean isFaceVisible(Direction face) {
            return (visibleMask & (1 << face.ordinal())) != 0;
        }

        public int face(Direction face) {
            return switch (face) {
                case DOWN -> down;
                case UP -> up;
                case NORTH -> north;
                case SOUTH -> south;
                case WEST -> west;
                case EAST -> east;
            };
        }
    }
}
