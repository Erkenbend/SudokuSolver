package oldmodernized;

public class CheckList {

    private final boolean[] presence = new boolean[9];

    public CheckList() {
        for (int i = 0; i < 9; i++) {
            this.presence[i] = false;
        }
    }

    public boolean isEveryoneThere() {
        for (boolean b : this.presence) {
            if (!b) return false;
        }
        return true;
    }

    public void setPresent(int v) {
        if (v < 1 || v > 9) {
            System.err.println("Something went wrong, numbers are supposed to be between 1 and 9");
            System.exit(1);
        } else {
            this.presence[v - 1] = true;
        }
    }
}
