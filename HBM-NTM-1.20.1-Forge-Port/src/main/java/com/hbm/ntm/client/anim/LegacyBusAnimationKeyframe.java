package com.hbm.ntm.client.anim;

public class LegacyBusAnimationKeyframe {
    public enum IType {
        CONSTANT,
        LINEAR,
        SIN_UP,
        SIN_DOWN,
        SIN_FULL,
        BEZIER,
        SINE,
        QUAD,
        CUBIC,
        QUART,
        QUINT,
        EXPO,
        CIRC,
        BOUNCE,
        ELASTIC,
        BACK
    }

    public enum EType {
        AUTO,
        EASE_IN,
        EASE_OUT,
        EASE_IN_OUT
    }

    public enum HType {
        FREE,
        ALIGNED,
        VECTOR,
        AUTO,
        AUTO_CLAMPED
    }

    private static final double POW_MIN = 0.0009765625F;
    private static final double POW_SCALE = 1.0F / (1.0F - 0.0009765625F);

    public double value;
    public IType interpolationType;
    public EType easingType;
    public int originalDuration;
    public int duration;
    public double leftX;
    public double leftY;
    public HType leftType;
    public double rightX;
    public double rightY;
    public HType rightType;
    public double amplitude;
    public double period;
    public double back;

    public LegacyBusAnimationKeyframe() {
        this.value = 0.0D;
        this.originalDuration = this.duration = 1;
        this.interpolationType = IType.LINEAR;
        this.easingType = EType.AUTO;
    }

    public LegacyBusAnimationKeyframe(double value, int duration) {
        this();
        this.value = value;
        this.originalDuration = this.duration = duration;
    }

    public LegacyBusAnimationKeyframe(double value, int duration, IType interpolation) {
        this(value, duration);
        this.interpolationType = interpolation;
    }

    public LegacyBusAnimationKeyframe(double value, int duration, IType interpolation, EType easing) {
        this(value, duration, interpolation);
        this.easingType = easing;
    }

    public double interpolate(double startTime, double currentTime, LegacyBusAnimationKeyframe previous) {
        if (previous == null) {
            previous = new LegacyBusAnimationKeyframe();
        }

        double a = value;
        double b = previous.value;
        double t = time(startTime, currentTime, duration);

        double begin = previous.value;
        double change = value - previous.value;
        double time = currentTime - startTime;

        if (Math.abs(previous.value - value) < 0.000001D) {
            return value;
        }

        if (previous.interpolationType == IType.BEZIER) {
            double v1x = startTime;
            double v1y = previous.value;
            double v2x = previous.rightX;
            double v2y = previous.rightY;
            double v3x = leftX;
            double v3y = leftY;
            double v4x = startTime + duration;
            double v4y = value;

            double h1x = v1x - v2x;
            double h1y = v1y - v2y;
            double h2x = v4x - v3x;
            double h2y = v4y - v3y;
            double len = v4x - v1x;
            double len1 = Math.abs(h1x);
            double len2 = Math.abs(h2x);

            if (len1 + len2 != 0.0D) {
                if (len1 > len) {
                    double fac = len / len1;
                    v2x = v1x - fac * h1x;
                    v2y = v1y - fac * h1y;
                }

                if (len2 > len) {
                    double fac = len / len2;
                    v3x = v4x - fac * h2x;
                    v3y = v4y - fac * h2y;
                }
            }

            double curveT = findZero(currentTime, v1x, v2x, v3x, v4x);
            return cubicBezier(v1y, v2y, v3y, v4y, curveT);
        } else if (previous.interpolationType == IType.BACK) {
            return switch (previous.easingType) {
                case EASE_IN -> easingBackEaseIn(time, begin, change, duration, previous.back);
                case EASE_IN_OUT -> easingBackEaseInOut(time, begin, change, duration, previous.back);
                default -> easingBackEaseOut(time, begin, change, duration, previous.back);
            };
        } else if (previous.interpolationType == IType.BOUNCE) {
            return switch (previous.easingType) {
                case EASE_IN -> easingBounceEaseIn(time, begin, change, duration);
                case EASE_IN_OUT -> easingBounceEaseInOut(time, begin, change, duration);
                default -> easingBounceEaseOut(time, begin, change, duration);
            };
        } else if (previous.interpolationType == IType.CIRC) {
            return switch (previous.easingType) {
                case EASE_OUT -> easingCircEaseOut(time, begin, change, duration);
                case EASE_IN_OUT -> easingCircEaseInOut(time, begin, change, duration);
                default -> easingCircEaseIn(time, begin, change, duration);
            };
        } else if (previous.interpolationType == IType.CUBIC) {
            return switch (previous.easingType) {
                case EASE_OUT -> easingCubicEaseOut(time, begin, change, duration);
                case EASE_IN_OUT -> easingCubicEaseInOut(time, begin, change, duration);
                default -> easingCubicEaseIn(time, begin, change, duration);
            };
        } else if (previous.interpolationType == IType.ELASTIC) {
            return switch (previous.easingType) {
                case EASE_IN -> easingElasticEaseIn(time, begin, change, duration, previous.amplitude, previous.period);
                case EASE_IN_OUT -> easingElasticEaseInOut(time, begin, change, duration, previous.amplitude, previous.period);
                default -> easingElasticEaseOut(time, begin, change, duration, previous.amplitude, previous.period);
            };
        } else if (previous.interpolationType == IType.EXPO) {
            return switch (previous.easingType) {
                case EASE_OUT -> easingExpoEaseOut(time, begin, change, duration);
                case EASE_IN_OUT -> easingExpoEaseInOut(time, begin, change, duration);
                default -> easingExpoEaseIn(time, begin, change, duration);
            };
        } else if (previous.interpolationType == IType.QUAD) {
            return switch (previous.easingType) {
                case EASE_OUT -> easingQuadEaseOut(time, begin, change, duration);
                case EASE_IN_OUT -> easingQuadEaseInOut(time, begin, change, duration);
                default -> easingQuadEaseIn(time, begin, change, duration);
            };
        } else if (previous.interpolationType == IType.QUART) {
            return switch (previous.easingType) {
                case EASE_OUT -> easingQuartEaseOut(time, begin, change, duration);
                case EASE_IN_OUT -> easingQuartEaseInOut(time, begin, change, duration);
                default -> easingQuartEaseIn(time, begin, change, duration);
            };
        } else if (previous.interpolationType == IType.QUINT) {
            return switch (previous.easingType) {
                case EASE_OUT -> easingQuintEaseOut(time, begin, change, duration);
                case EASE_IN_OUT -> easingQuintEaseInOut(time, begin, change, duration);
                default -> easingQuintEaseIn(time, begin, change, duration);
            };
        } else if (previous.interpolationType == IType.SINE) {
            return switch (previous.easingType) {
                case EASE_OUT -> easingSineEaseOut(time, begin, change, duration);
                case EASE_IN_OUT -> easingSineEaseInOut(time, begin, change, duration);
                default -> easingSineEaseIn(time, begin, change, duration);
            };
        }

        return (a - b) * t + b;
    }

    private double sqrt3(double d) {
        if (d > 0.000001D) {
            return Math.exp(Math.log(d) / 3.0D);
        } else if (d > -0.000001D) {
            return 0.0D;
        } else {
            return -Math.exp(Math.log(-d) / 3.0D);
        }
    }

    private double time(double start, double end, double duration) {
        if (interpolationType == IType.SIN_UP) {
            return -Math.sin(((end - start) / duration * Math.PI + Math.PI) / 2.0D) + 1.0D;
        }
        if (interpolationType == IType.SIN_DOWN) {
            return Math.sin((end - start) / duration * Math.PI / 2.0D);
        }
        if (interpolationType == IType.SIN_FULL) {
            return (-Math.cos((end - start) / duration * Math.PI) + 1.0D) / 2.0D;
        }
        return (end - start) / duration;
    }

    private double solveCubic(double c0, double c1, double c2, double c3) {
        if (c3 > 0.000001D || c3 < -0.000001D) {
            double a = c2 / c3;
            double b = c1 / c3;
            double c = c0 / c3;
            a = a / 3.0D;

            double p = b / 3.0D - a * a;
            double q = (2.0D * a * a * a - a * b + c) / 2.0D;
            double d = q * q + p * p * p;

            if (d > 0.000001D) {
                double t = Math.sqrt(d);
                return sqrt3(-q + t) + sqrt3(-q - t) - a;
            } else if (d > -0.000001D) {
                double t = sqrt3(-q);
                double result = 2.0D * t - a;
                if (result < 0.000001D || result > 1.000001D) {
                    result = -t - a;
                }
                return result;
            }

            double phi = Math.acos(-q / Math.sqrt(-(p * p * p)));
            double t = Math.sqrt(-p);
            p = Math.cos(phi / 3.0D);
            q = Math.sqrt(3.0D - 3.0D * p * p);
            double result = 2.0D * t * p - a;
            if (result < 0.000001D || result > 1.000001D) {
                result = -t * (p + q) - a;
            }
            if (result < 0.000001D || result > 1.000001D) {
                result = -t * (p - q) - a;
            }
            return result;
        }

        double a = c2;
        double b = c1;
        double c = c0;

        if (a > 0.000001D) {
            double p = b * b - 4.0D * a * c;

            if (p > 0.000001D) {
                p = Math.sqrt(p);
                double result = (-b - p) / (2.0D * a);
                if (result < 0.000001D || result > 1.000001D) {
                    result = (-b + p) / (2.0D * a);
                }
                return result;
            } else if (p > -0.000001D) {
                return -b / (2.0D * a);
            }
        }

        if (b > 0.000001D) {
            return -c / b;
        }

        return 0.0D;
    }

    private double findZero(double t, double x1, double x2, double x3, double x4) {
        double c0 = x1 - t;
        double c1 = 3.0F * (x2 - x1);
        double c2 = 3.0F * (x1 - 2.0F * x2 + x3);
        double c3 = x4 - x1 + 3.0F * (x2 - x3);

        return solveCubic(c0, c1, c2, c3);
    }

    private double cubicBezier(double y1, double y2, double y3, double y4, double t) {
        double c0 = y1;
        double c1 = 3.0F * (y2 - y1);
        double c2 = 3.0F * (y1 - 2.0F * y2 + y3);
        double c3 = y4 - y1 + 3.0F * (y2 - y3);

        return c0 + t * c1 + t * t * c2 + t * t * t * c3;
    }

    double easingBackEaseIn(double time, double begin, double change, double duration, double overshoot) {
        time /= duration;
        return change * time * time * ((overshoot + 1.0D) * time - overshoot) + begin;
    }

    double easingBackEaseOut(double time, double begin, double change, double duration, double overshoot) {
        time = time / duration - 1.0D;
        return change * (time * time * ((overshoot + 1.0D) * time + overshoot) + 1.0D) + begin;
    }

    double easingBackEaseInOut(double time, double begin, double change, double duration, double overshoot) {
        overshoot *= 1.525F;
        if ((time /= duration / 2.0D) < 1.0D) {
            return change / 2.0D * (time * time * ((overshoot + 1.0D) * time - overshoot)) + begin;
        }
        time -= 2.0D;
        return change / 2.0D * (time * time * ((overshoot + 1.0D) * time + overshoot) + 2.0D) + begin;
    }

    double easingBounceEaseOut(double time, double begin, double change, double duration) {
        time /= duration;
        if (time < (1.0D / 2.75F)) {
            return change * (7.5625F * time * time) + begin;
        }
        if (time < (2.0D / 2.75F)) {
            time -= (1.5F / 2.75F);
            return change * ((7.5625F * time) * time + 0.75F) + begin;
        }
        if (time < (2.5D / 2.75F)) {
            time -= (2.25F / 2.75F);
            return change * ((7.5625F * time) * time + 0.9375F) + begin;
        }
        time -= (2.625F / 2.75F);
        return change * ((7.5625F * time) * time + 0.984375F) + begin;
    }

    double easingBounceEaseIn(double time, double begin, double change, double duration) {
        return change - easingBounceEaseOut(duration - time, 0.0D, change, duration) + begin;
    }

    double easingBounceEaseInOut(double time, double begin, double change, double duration) {
        if (time < duration / 2.0D) {
            return easingBounceEaseIn(time * 2.0D, 0.0D, change, duration) * 0.5F + begin;
        }
        return easingBounceEaseOut(time * 2.0D - duration, 0.0D, change, duration) * 0.5F + change * 0.5F + begin;
    }

    double easingCircEaseIn(double time, double begin, double change, double duration) {
        time /= duration;
        return -change * (Math.sqrt(1.0D - time * time) - 1.0D) + begin;
    }

    double easingCircEaseOut(double time, double begin, double change, double duration) {
        time = time / duration - 1.0D;
        return change * Math.sqrt(1.0D - time * time) + begin;
    }

    double easingCircEaseInOut(double time, double begin, double change, double duration) {
        if ((time /= duration / 2.0D) < 1.0D) {
            return -change / 2.0D * (Math.sqrt(1.0D - time * time) - 1.0D) + begin;
        }
        time -= 2.0D;
        return change / 2.0D * (Math.sqrt(1.0D - time * time) + 1.0D) + begin;
    }

    double easingCubicEaseIn(double time, double begin, double change, double duration) {
        time /= duration;
        return change * time * time * time + begin;
    }

    double easingCubicEaseOut(double time, double begin, double change, double duration) {
        time = time / duration - 1.0D;
        return change * (time * time * time + 1.0D) + begin;
    }

    double easingCubicEaseInOut(double time, double begin, double change, double duration) {
        if ((time /= duration / 2.0D) < 1.0D) {
            return change / 2.0D * time * time * time + begin;
        }
        time -= 2.0D;
        return change / 2.0D * (time * time * time + 2.0D) + begin;
    }

    double elasticBlend(double time, double change, double duration, double amplitude, double s, double f) {
        if (change != 0.0D) {
            double t = Math.abs(s);
            if (amplitude != 0.0D) {
                f *= amplitude / Math.abs(change);
            } else {
                f = 0.0F;
            }

            if (Math.abs(time * duration) < t) {
                double l = Math.abs(time * duration) / t;
                f = (f * l) + (1.0F - l);
            }
        }

        return f;
    }

    double easingElasticEaseIn(double time, double begin, double change, double duration, double amplitude, double period) {
        double s;
        double f = 1.0F;

        if (time == 0.0D) {
            return begin;
        }

        if ((time /= duration) == 1.0D) {
            return begin + change;
        }
        time -= 1.0D;
        if (period == 0.0D) {
            period = duration * 0.3F;
        }
        if (amplitude == 0.0D || amplitude < Math.abs(change)) {
            s = period / 4.0D;
            f = elasticBlend(time, change, duration, amplitude, s, f);
            amplitude = change;
        } else {
            s = period / (2.0D * Math.PI) * Math.asin(change / amplitude);
        }

        return (-f * (amplitude * Math.pow(2.0D, 10.0D * time) * Math.sin((time * duration - s) * (2.0D * Math.PI) / period))) + begin;
    }

    double easingElasticEaseOut(double time, double begin, double change, double duration, double amplitude, double period) {
        double s;
        double f = 1.0F;

        if (time == 0.0D) {
            return begin;
        }
        if ((time /= duration) == 1.0D) {
            return begin + change;
        }
        time = -time;
        if (period == 0.0D) {
            period = duration * 0.3F;
        }
        if (amplitude == 0.0D || amplitude < Math.abs(change)) {
            s = period / 4.0D;
            f = elasticBlend(time, change, duration, amplitude, s, f);
            amplitude = change;
        } else {
            s = period / (2.0D * Math.PI) * Math.asin(change / amplitude);
        }

        return (f * (amplitude * Math.pow(2.0D, 10.0D * time) * Math.sin((time * duration - s) * (2.0D * Math.PI) / period))) + change + begin;
    }

    double easingElasticEaseInOut(double time, double begin, double change, double duration, double amplitude, double period) {
        double s;
        double f = 1.0F;

        if (time == 0.0D) {
            return begin;
        }
        if ((time /= duration / 2.0D) == 2.0D) {
            return begin + change;
        }
        time -= 1.0D;
        if (period == 0.0D) {
            period = duration * (0.3F * 1.5F);
        }
        if (amplitude == 0.0D || amplitude < Math.abs(change)) {
            s = period / 4.0D;
            f = elasticBlend(time, change, duration, amplitude, s, f);
            amplitude = change;
        } else {
            s = period / (2.0D * Math.PI) * Math.asin(change / amplitude);
        }

        if (time < 0.0D) {
            f *= -0.5F;
            return (f * (amplitude * Math.pow(2.0D, 10.0D * time) * Math.sin((time * duration - s) * (2.0D * Math.PI) / period))) + begin;
        }

        time = -time;
        f *= 0.5F;
        return (f * (amplitude * Math.pow(2.0D, 10.0D * time) * Math.sin((time * duration - s) * (2.0D * Math.PI) / period))) + change + begin;
    }

    double easingExpoEaseIn(double time, double begin, double change, double duration) {
        if (time == 0.0D) {
            return begin;
        }
        return change * (Math.pow(2.0D, 10.0D * (time / duration - 1.0D)) - POW_MIN) * POW_SCALE + begin;
    }

    double easingExpoEaseOut(double time, double begin, double change, double duration) {
        if (time == 0.0D) {
            return begin;
        }
        return change * (1.0D - (Math.pow(2.0D, -10.0D * time / duration) - POW_MIN) * POW_SCALE) + begin;
    }

    double easingExpoEaseInOut(double time, double begin, double change, double duration) {
        double durationHalf = duration / 2.0F;
        double changeHalf = change / 2.0F;
        if (time <= durationHalf) {
            return easingExpoEaseIn(time, begin, changeHalf, durationHalf);
        }
        return easingExpoEaseOut(time - durationHalf, begin + changeHalf, changeHalf, durationHalf);
    }

    double easingLinearEase(double time, double begin, double change, double duration) {
        return change * time / duration + begin;
    }

    double easingQuadEaseIn(double time, double begin, double change, double duration) {
        time /= duration;
        return change * time * time + begin;
    }

    double easingQuadEaseOut(double time, double begin, double change, double duration) {
        time /= duration;
        return -change * time * (time - 2.0D) + begin;
    }

    double easingQuadEaseInOut(double time, double begin, double change, double duration) {
        if ((time /= duration / 2.0D) < 1.0D) {
            return change / 2.0D * time * time + begin;
        }
        time -= 1.0D;
        return -change / 2.0D * (time * (time - 2.0D) - 1.0D) + begin;
    }

    double easingQuartEaseIn(double time, double begin, double change, double duration) {
        time /= duration;
        return change * time * time * time * time + begin;
    }

    double easingQuartEaseOut(double time, double begin, double change, double duration) {
        time = time / duration - 1.0D;
        return -change * (time * time * time * time - 1.0D) + begin;
    }

    double easingQuartEaseInOut(double time, double begin, double change, double duration) {
        if ((time /= duration / 2.0D) < 1.0D) {
            return change / 2.0D * time * time * time * time + begin;
        }
        time -= 2.0D;
        return -change / 2.0D * (time * time * time * time - 2.0D) + begin;
    }

    double easingQuintEaseIn(double time, double begin, double change, double duration) {
        time /= duration;
        return change * time * time * time * time * time + begin;
    }

    double easingQuintEaseOut(double time, double begin, double change, double duration) {
        time = time / duration - 1.0D;
        return change * (time * time * time * time * time + 1.0D) + begin;
    }

    double easingQuintEaseInOut(double time, double begin, double change, double duration) {
        if ((time /= duration / 2.0D) < 1.0D) {
            return change / 2.0D * time * time * time * time * time + begin;
        }
        time -= 2.0D;
        return change / 2.0D * (time * time * time * time * time + 2.0D) + begin;
    }

    double easingSineEaseIn(double time, double begin, double change, double duration) {
        return -change * Math.cos(time / duration * Math.PI / 2.0D) + change + begin;
    }

    double easingSineEaseOut(double time, double begin, double change, double duration) {
        return change * Math.sin(time / duration * Math.PI / 2.0D) + begin;
    }

    double easingSineEaseInOut(double time, double begin, double change, double duration) {
        return -change / 2.0D * (Math.cos(Math.PI * time / duration) - 1.0D) + begin;
    }
}
