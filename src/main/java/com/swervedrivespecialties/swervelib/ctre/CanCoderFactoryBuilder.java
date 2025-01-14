package com.swervedrivespecialties.swervelib.ctre;

import com.ctre.phoenix.sensors.AbsoluteSensorRange;
import com.ctre.phoenix.sensors.CANCoder;
import com.ctre.phoenix.sensors.CANCoderConfiguration;
import com.ctre.phoenix.sensors.CANCoderStatusFrame;
import com.swervedrivespecialties.swervelib.AbsoluteEncoder;
import com.swervedrivespecialties.swervelib.AbsoluteEncoderFactory;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DutyCycle;

import edu.wpi.first.wpilibj.DriverStation;

public class CanCoderFactoryBuilder {
    private Direction direction = Direction.COUNTER_CLOCKWISE;
    private int periodMilliseconds = 10;

    public CanCoderFactoryBuilder withReadingUpdatePeriod(int periodMilliseconds) {
        this.periodMilliseconds = periodMilliseconds;
        return this;
    }

    public CanCoderFactoryBuilder withDirection(Direction direction) {
        this.direction = direction;
        return this;
    }

    public AbsoluteEncoderFactory<CanCoderAbsoluteConfiguration> build() {
        return configuration -> {
            //CANCoderConfiguration config = new CANCoderConfiguration();
            /*config.absoluteSensorRange = AbsoluteSensorRange.Unsigned_0_to_360;
            config.magnetOffsetDegrees = Math.toDegrees(configuration.getOffset());
            config.sensorDirection = direction == Direction.CLOCKWISE;

            CANCoder encoder = new CANCoder(configuration.getId());
            CtreUtils.checkCtreError(encoder.configAllSettings(config, 250), "Failed to configure CANCoder");

            CtreUtils.checkCtreError(encoder.setStatusFramePeriod(CANCoderStatusFrame.SensorData, periodMilliseconds, 250), "Failed to configure CANCoder update rate");

            return new EncoderImplementation(encoder);*/

            return new OrbitEncoderImplementation(configuration.getId(), configuration.getOffset());
        };
    }

    private static class EncoderImplementation implements AbsoluteEncoder {
        private final CANCoder encoder;

        private EncoderImplementation(CANCoder encoder) {
            this.encoder = encoder;
        }

        @Override
        public double getAbsoluteAngle() {
            double angle = Math.toRadians(encoder.getAbsolutePosition());
            angle %= 2.0 * Math.PI;
            if (angle < 0.0) {
                angle += 2.0 * Math.PI;
            }

            return angle;
        }
    }

    // This is for Encoder over PWM
    private static class OrbitEncoderImplementation implements AbsoluteEncoder {

        private final DigitalInput dio;
        private final DutyCycle encoder;

        private double offset;

        private OrbitEncoderImplementation(int port, double offset) {
            this.dio = new DigitalInput(port);
            this.encoder = new DutyCycle(this.dio);
            this.offset = offset;
        }

        @Override
        public double getAbsoluteAngle() {
            double angle = Math.toRadians(360.0 * encoder.getOutput()) + this.offset;

            angle %= 2.0 * Math.PI;

            if (angle < 0.0) {
                angle += 2.0 * Math.PI;
            }

            return angle;
        }
        
    }

    public enum Direction {
        CLOCKWISE,
        COUNTER_CLOCKWISE
    }
}
