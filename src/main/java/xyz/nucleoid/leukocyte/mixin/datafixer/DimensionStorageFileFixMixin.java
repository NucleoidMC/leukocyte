package xyz.nucleoid.leukocyte.mixin.datafixer;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.util.filefix.fixes.DimensionStorageFileFix;
import net.minecraft.util.filefix.operations.FileFixOperations;
import net.minecraft.util.filefix.operations.Move;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;
import xyz.nucleoid.leukocyte.Leukocyte;

import java.util.ArrayList;
import java.util.List;

@Mixin(DimensionStorageFileFix.class)
public class DimensionStorageFileFixMixin {

    @ModifyExpressionValue(
            method = "makeFixer",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;of(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/util/List;",
                    ordinal = 0
            ),
            slice = @Slice(
                    from = @At(
                            value = "FIELD",
                            target = "Lnet/minecraft/util/filefix/access/FileRelation;DATA:Lnet/minecraft/util/filefix/access/FileRelation;",
                            opcode = Opcodes.GETSTATIC
                    )
            )
    )
    public List<Move> updateLeukocyteLocation(List<@NotNull Move> original) {
        List<Move> list = new ArrayList<>(original);
        list.add(FileFixOperations.move("leukocyte.dat", Leukocyte.ID.getNamespace() + "/" + Leukocyte.ID.getPath() + ".dat"));
        return list;
    }

}
