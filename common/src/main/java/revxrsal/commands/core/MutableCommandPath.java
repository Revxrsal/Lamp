package revxrsal.commands.core;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.ArgumentStack;

import java.util.Iterator;

public class MutableCommandPath extends CommandPath {

    public static @NotNull MutableCommandPath empty() {
        return new MutableCommandPath(new String[0]);
    }

    public MutableCommandPath(String[] path) {
        super(path);
    }

    public MutableCommandPath(ArgumentStack argumentStack) {
        super(argumentStack.toArray(new String[0]));
    }

    public String removeFirst() {return path.removeFirst();}

    public String removeLast() {return path.removeLast();}

    public void addFirst(String s) {path.addFirst(s);}

    public void addLast(String s) {path.addLast(s);}

    public boolean contains(Object o) {return path.contains(o);}

    public boolean add(String s) {return path.add(s);}

    public void clear() {path.clear();}

    public void add(int index, String element) {path.add(index, element);}

    public String peek() {return path.peek();}

    public String poll() {return path.poll();}

    public void push(String s) {path.push(s);}

    public String pop() {return path.pop();}

    public CommandPath toImmutablePath() {
        return new CommandPath(path.toArray(new String[0]));
    }

    @Override public @NotNull Iterator<String> iterator() {
        return path.iterator();
    }

    @Override public boolean isMutable() {return true;}
}
