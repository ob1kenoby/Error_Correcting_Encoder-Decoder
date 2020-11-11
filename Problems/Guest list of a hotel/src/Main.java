import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Scanner;

class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Deque<String> names = new ArrayDeque<>();
        while (scanner.hasNext()) {
            String[] input = scanner.nextLine().split(" ");
            for (String name : input) {
                names.offerLast(name);
            }
        }
        scanner.close();

        while (!names.isEmpty()) {
            System.out.println(names.pollLast());
        }
    }
}