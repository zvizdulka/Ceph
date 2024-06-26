package ulyana.MDS;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

//inode типа директория
public class InodeDirectory extends Inode implements Serializable {
    final private ArrayList<Inode> nodes;//ссылки на потомков

    public InodeDirectory(String nameInode, String path, int numberInode) {
        super(nameInode, path, numberInode, 1);
        nodes = new ArrayList<>();
    }

    //добавить потомка
    public void addInode(Inode inode) {
        nodes.add(inode);
    }

    //количество потомков
    public int size() {
        return nodes.size();
    }

    public Inode get(int i) {
        return nodes.get(i);
    }

    public InodeDirectory searchDirectory(String nameDirectory) {
        for (Inode i:nodes) {
            if (i.toString().equals(nameDirectory) && i.getType() == 1)
                return (InodeDirectory) i;
        }
        return null;
    }

    public InodeFile searchFile(String nameDirectory) {
        for (Inode i:nodes) {
            if (i.toString().equals(nameDirectory) && i.getType() == 0)
                return (InodeFile) i;
        }
        return null;
    }

    //удалить потомка по id
    public void delete(int id) {
        int i = 0;
        for (; i < nodes.size(); i++) {
            if (nodes.get(i).getID() == id) {
                nodes.remove(i);
                break;
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InodeDirectory inode = (InodeDirectory) o;
        return nodes.equals(inode.nodes) && getID() == inode.getID() && getName().equals(inode.getName())
                && getLayout().equals(inode.getLayout()) && getType() == inode.getType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodes);
    }

    //удалить всех потомков
    public void removeAll() {
        nodes.clear();
    }

    //получить всех потомков
    //используется только в тестах
    public ArrayList<Inode> getNodes() {
        return nodes;
    }
}
