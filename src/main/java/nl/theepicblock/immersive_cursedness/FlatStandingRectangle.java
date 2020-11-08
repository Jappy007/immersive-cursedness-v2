package nl.theepicblock.immersive_cursedness;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class FlatStandingRectangle {
    //Right is defined as the most positive point in whatever axis this is
    protected final double top,bottom,left,right,other;
    protected final Direction.Axis axis;

    public FlatStandingRectangle(double top, double bottom, double left, double right, double other, Direction.Axis axis) {
        this.top = top;
        this.bottom = bottom;
        this.left = left;
        this.right = right;
        this.other = other;
        this.axis = axis;
    }

    public Vec3d getBottomRight() {
        return createVec3d(bottom, right);
    }

    public Vec3d getBottomLeft() {
        return createVec3d(bottom, left);
    }

    public Vec3d getTopRight() {
        return createVec3d(top, right);
    }

    public Vec3d getTopLeft() {
        return createVec3d(top, left);
    }

    public BlockPos getBottomLeftBlock() {
        return createBlockPos(bottom, left);
    }

    public BlockPos getTopRightBlock() {
        return createBlockPos(top-1, right-1);
    }

    public BlockPos getBottomLeftBlockClamped(Vec3d center, int limit) {
        double centerP = Util.get(center, Util.rotate(axis));
        return createBlockPos(clamp(bottom,0,255), clamp(left,centerP-limit,centerP+limit));
    }

    public BlockPos getTopRightBlockClamped(Vec3d center, int limit) {
        double centerP = Util.get(center, Util.rotate(axis));
        return createBlockPos(clamp(top-1,0,255), clamp(right-1,centerP-limit,centerP+limit));
    }

    public FlatStandingRectangle expand(int i, Vec3d source) {
        double distance = Util.get(source, this.axis)-this.other;
        double sourceForPrimaryAxis = Util.get(source,Util.rotate(axis));
        double newOther = this.other<Util.get(source,axis) ? this.other-i : this.other+i;
        double newDistance = Util.get(source, this.axis)-newOther;
        return new FlatStandingRectangle(
                source.y+(this.top-source.y)/distance*newDistance,
                source.y+(this.bottom-source.y)/distance*newDistance,
                sourceForPrimaryAxis+(this.left-sourceForPrimaryAxis)/distance*newDistance,
                sourceForPrimaryAxis+(this.right-sourceForPrimaryAxis)/distance*newDistance,
                newOther,
                this.axis
        );
    }

    public boolean contains(Vec3d pos) {
        return  pos.y >= this.bottom &&
                pos.y <= this.top &&
                Util.get(pos, Util.rotate(axis)) >= this.left &&
                Util.get(pos, Util.rotate(axis)) <= this.right &&
                Util.get(pos, axis) <= this.other+1 &&
                Util.get(pos, axis) >= this.other;
    }

    public boolean isBeside(Vec3d pos) {
        return  Util.get(pos, axis) < this.other+1 &&
                Util.get(pos, axis) > this.other;
    }

    public void iterateClamped(Vec3d center, int limit, Consumer<BlockPos> predicate) {
        double centerP = Util.get(center, Util.rotate(axis));
        int left = (int)Math.round(clamp(this.left,centerP-limit,centerP+limit));
        int right = (int)Math.round(clamp(this.right-1,centerP-limit,centerP+limit));
        int top = (int)Math.round(clamp(this.top-1,0,255));
        int bottom = (int)Math.round(clamp(this.bottom,0,255));

        if (left == right) return;

        Direction.Axis otherAxis = Util.rotate(axis);
        BlockPos.Mutable mutPos = new BlockPos.Mutable();
        Util.set(mutPos, (int)Math.round(other), axis);

        for (int y = bottom; y <= top; y++) {
            mutPos.setY(y);
            for (int o = left; o <= right; o++) {
                Util.set(mutPos, o, otherAxis);
                predicate.accept(mutPos);
            }
        }
    }

    private Vec3d createVec3d(double y, double primaryAxis) {
        if (axis == Direction.Axis.X) {
            return new Vec3d(other, y, primaryAxis);
        } else {
            return new Vec3d(primaryAxis, y, other);
        }
    }

    private BlockPos createBlockPos(double y, double primaryAxis) {
        if (axis == Direction.Axis.X) {
            return Util.makeBlockPos(other, y, primaryAxis);
        } else {
            return Util.makeBlockPos(primaryAxis, y, other);
        }
    }

    private double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }
}
