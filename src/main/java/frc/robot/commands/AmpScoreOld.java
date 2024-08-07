// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import frc.robot.subsystems.Limelight;
import frc.robot.subsystems.Pivot;
import frc.robot.subsystems.Shooter;
import frc.robot.utils.GlobalsValues.PivotGlobalValues;

// NOTE:  Consider using this command inline, rather than writing a subclass.  For more
// information, see:
// https://docs.wpilib.org/en/stable/docs/software/commandbased/convenience-features.html
public class AmpScoreOld extends SequentialCommandGroup {
  /** Creates a new ShootRing. */
  private Shooter shooter;
  private Pivot pivot;
  private Limelight limelight;

  public AmpScoreOld(Shooter shooter, Pivot pivot, Limelight limelight) {
    this.shooter = shooter;
    this.pivot = pivot;
    this.limelight = limelight;
    addRequirements(shooter, pivot, limelight);

    // Add your commands in the addCommands() call, e.g.
    // addCommands(new FooCommand(), new BarCommand());
    addCommands(
        new ParallelCommandGroup(
            new SetPivot(pivot, 52.5).withTimeout(1),
            new AmpRampUpOld(shooter, limelight).withTimeout(1)),
        new PushRingAmp(shooter, limelight).withTimeout(0.3),
        new StopShooter(shooter).withTimeout(0.05),
        new SetPivot(pivot, PivotGlobalValues.PIVOT_NEUTRAL_ANGLE).withTimeout(0.5));
  }
}
