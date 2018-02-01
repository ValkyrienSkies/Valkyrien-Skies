/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2017 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package code.elix_x.excomms.asm.transform.specific;

import code.elix_x.excomms.asm.transform.OpcodeGroups;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class SpecificMethodNodeTransformer extends SpecificNodeTransformer<MethodNode> {

    private final String target;

    public SpecificMethodNodeTransformer(String target, int priority, Function<MethodNode, MethodNode> transform) {
        super(MethodNode.class, priority, transform);
        this.target = target;
    }

    public SpecificMethodNodeTransformer(int priority, String target, Consumer<MethodNode> transform) {
        super(priority, MethodNode.class, transform);
        this.target = target;
    }

    public static SpecificMethodNodeTransformer instructionsTransformer(String target, int priority, Function<InsnList, InsnList> insnOld2new) {
        return new SpecificMethodNodeTransformer(priority, target, node -> node.instructions = insnOld2new.apply(node.instructions));
    }

    public static SpecificMethodNodeTransformer instructionsTransformer(int priority, String target, Consumer<InsnList> insnConsumer) {
        return new SpecificMethodNodeTransformer(priority, target, node -> insnConsumer.accept(node.instructions));
    }

    public static SpecificMethodNodeTransformer instructionsBeginningInserter(String target, int priority, InsnList insn) {
        return instructionsTransformer(priority, target, instructions -> instructions.insert(insn));
    }

    public static SpecificMethodNodeTransformer instructionsInserter(String target, int priority, Function<InsnList, AbstractInsnNode> where, InsnList insn, boolean before) {
        return instructionsTransformer(priority, target, instructions -> {
            if (before) instructions.insertBefore(where.apply(instructions), insn);
            else instructions.insert(where.apply(instructions), insn);
        });
    }

    public static SpecificMethodNodeTransformer instructionsInserter(String target, int priority, Function<AbstractInsnNode, Pair<InsnList, Boolean>> inserter) {
        return instructionsTransformer(priority, target, instructions -> {
            for (AbstractInsnNode node : instructions.toArray()) {
                Pair<InsnList, Boolean> pair = inserter.apply(node);
                if (pair != null) if (pair.getRight()) instructions.insertBefore(node, pair.getLeft());
                else instructions.insert(node, pair.getLeft());
            }
        });
    }

    public static SpecificMethodNodeTransformer instructionsInserterBeforeReturn(String target, int priority, Supplier<InsnList> insn) {
        return instructionsInserter(target, priority, node -> OpcodeGroups.RETURN.contains(node.getOpcode()) ? new ImmutablePair<>(insn.get(), true) : null);
    }

    public static SpecificMethodNodeTransformer instructionsNodesTransformer(String target, int priority, Function<AbstractInsnNode, AbstractInsnNode> insnOld2new) {
        return instructionsTransformer(priority, target, instructions -> {
            for (AbstractInsnNode node : instructions.toArray()) {
                AbstractInsnNode res = insnOld2new.apply(node);
                if (res != null) instructions.set(node, res);
                else instructions.remove(node);
            }
        });
    }

    @Override
    public boolean accepts(MethodNode target) {
        return target != null && (this.target.contains("(") ? this.target.equals(target.name + target.desc) : this.target.equals(target.name));
    }

}
