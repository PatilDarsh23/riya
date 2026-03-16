import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * DSAVisualizerFull.java
 *
 * Single-file DSA Visualizer using Swing (no Canvas).
 * Includes: DFS, BFS, Heap, PriorityQueue, HeapSort,
 * Tree traversals (pre/in/post), Bubble, Quick, Merge Sort,
 * Stack, Queue, Linked List (Basic Ops), Linear Search, Binary Search.
 *
 * Algorithms run in SwingWorker background tasks to avoid UI freeze.
 */
public class DSAVisualizerFull extends JFrame 
{

    private final JTextArea outputArea = new JTextArea();
    private final JTextArea pseudocodeArea = new JTextArea();
    private final JComboBox<String> algoSelector;
    private final JButton runButton = new JButton("Run");
    private SwingWorker<?, ?> currentWorker = null;

    public DSAVisualizerFull() 
    {
        setTitle("DSA Visualizer — Full (No Canvas)");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLayout(new BorderLayout());

        // Top controls
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        // --- NEW ALGO ADDITIONS ---
        algoSelector = new JComboBox<>
        (new String[]
        {
                "Depth First Search (DFS)",
                "Breadth First Search (BFS)",
                "Linear Search",
                "Binary Search (Sorted Array)",
                "Bubble Sort",
                "Quick Sort",
                "Merge Sort",
                "Heap Sort",
                "Heap Construction (Build Max Heap)",
                "Stack Demo (Push/Pop)",
                "Queue Demo (Enqueue/Dequeue)",
                "Priority Queue Demo",
                "Linked List Demo (Insert/Delete/Traverse)",
                "Binary Tree - Preorder",
                "Binary Tree - Inorder",
                "Binary Tree - Postorder",
    });
        // --------------------------
        JButton stopButton = new JButton("Stop");
        top.add(new JLabel("Algorithm:"));
        top.add(algoSelector);
        top.add(runButton);
        top.add(stopButton);

        // Output and pseudocode panels
        outputArea.setEditable(false);
        outputArea.setBorder(new TitledBorder("Algorithm Tracing / Output"));
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        pseudocodeArea.setEditable(false);
        pseudocodeArea.setBorder(new TitledBorder("Pseudocode & Highlights"));
        pseudocodeArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(outputArea), new JScrollPane(pseudocodeArea));
        split.setDividerLocation(520);

        add(top, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);

        // Actions
        runButton.addActionListener(e -> startSelectedAlgorithm());
        stopButton.addActionListener(e -> stopWorker());

        // key: double-click clears
        outputArea.addMouseListener(new MouseAdapter()
        {
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2) outputArea.setText("");
            }
        });

        pseudocodeArea.addMouseListener(new MouseAdapter()
        {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) pseudocodeArea.setText("");
            }
        });
    }

    // ======= Start / Stop =======
    private void startSelectedAlgorithm()
    {
        if (currentWorker != null && !currentWorker.isDone())
        {
            appendOutput("An algorithm is already running. Stop it first to start another.\n");
            return;
        }
        String selected = (String) algoSelector.getSelectedItem();
        outputArea.setText("");
        pseudocodeArea.setText("");
        switch (selected) {
            case "Depth First Search (DFS)" -> runDFS();
            case "Breadth First Search (BFS)" -> runBFS();
            case "Linear Search" -> runLinearSearch();
            case "Binary Search (Sorted Array)" -> runBinarySearch();
            case "Bubble Sort" -> runBubbleSort();
            case "Quick Sort" -> runQuickSort();
            case "Merge Sort" -> runMergeSort();
            case "Heap Sort" -> runHeapSort();
            case "Heap Construction (Build Max Heap)" -> runBuildHeap();
            case "Stack Demo (Push/Pop)" -> runStackDemo();
            case "Queue Demo (Enqueue/Dequeue)" -> runQueueDemo();
            case "Priority Queue Demo" -> runPriorityQueue();
            case "Linked List Demo (Insert/Delete/Traverse)" -> runLinkedListDemo();
            case "Binary Tree - Preorder" -> runTreeTraversal("PREORDER");
            case "Binary Tree - Inorder" -> runTreeTraversal("INORDER");
            case "Binary Tree - Postorder" -> runTreeTraversal("POSTORDER");
            // -------------------------
            default -> appendOutput("Not implemented.\n");
        }
    }

    private void stopWorker()
    {
        if (currentWorker != null && !currentWorker.isDone())
        {
            currentWorker.cancel(true);
            appendOutput("Requested stop. Worker will cancel at next check.\n");
        } else
        {
            appendOutput("No running algorithm to stop.\n");
        }
    }

    // ======= Utility UI update helpers (thread-safe) =======
    private void appendOutput(String text)
    {
        SwingUtilities.invokeLater(() ->
        {
            outputArea.append(text);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        });
    }

    private void setPseudocode(String code)
    {
        SwingUtilities.invokeLater(() ->
        {
            pseudocodeArea.setText(code + "\n");
            pseudocodeArea.setCaretPosition(0);
        });
    }

    private void highlightStep(String step)
    {
        SwingUtilities.invokeLater(() ->
        {
            pseudocodeArea.append("> " + step + "\n");
            pseudocodeArea.setCaretPosition(pseudocodeArea.getDocument().getLength());
        });
    }

    private void sleepInterruptible(int ms) throws InterruptedException
    {
        // break into small sleeps so SwingWorker cancel can be responsive
        int chunk = 50;
        int done = 0;
        while (done < ms)
        {
            if (Thread.currentThread().isInterrupted()) throw new InterruptedException();
            Thread.sleep(Math.min(chunk, ms - done));
            done += chunk;
        }
    }

    // ======= Algorithms implemented as SwingWorkers (EXISTING CODE) =======

    // ---------- DFS ----------
    private void runDFS()
    {
        setPseudocode("""
                procedure DFS(G, v):
                    mark v as visited
                    for each neighbor u of v:
                        if u not visited:
                            DFS(G, u)
                """);
        // sample graph adjacency list
        Map<Integer, List<Integer>> graph = Map.of(
                0, List.of(1, 2),
                1, List.of(0, 3, 4),
                2, List.of(0, 5),
                3, List.of(1),
                4, List.of(1),
                5, List.of(2)
        );

        currentWorker = new SwingWorker<Void, String>()
        {
            boolean[] visited = new boolean[6];
            @Override protected Void doInBackground()
            {
                try
                {
                    dfs(0);
                } 
                catch (InterruptedException ex)
                {
                    publish("DFS interrupted.\n");
                }
                return null;
            }
            private void dfs(int v) throws InterruptedException
            {
                if (isCancelled()) throw new InterruptedException();
                visited[v] = true;
                publish("Visited: " + v + "\n");
                highlightStep("visit " + v);
                sleepInterruptible(600);
                for (int u : graph.getOrDefault(v, List.of()))
                {
                    if (isCancelled()) throw new InterruptedException();
                    if (!visited[u])
                    {
                        highlightStep("DFS(G, " + u + ")");
                        dfs(u);
                    } else
                    {
                        publish("Already visited " + u + "\n");
                    }
                }
            }
            @Override protected void process(List<String> chunks)
            {
                for (String s : chunks) appendOutput(s);
            }
            @Override protected void done()
            { 
                appendOutput("DFS completed.\n");
            }
        };
        currentWorker.execute();
    }

    // ---------- BFS ----------
    private void runBFS()
    {
        setPseudocode("""
                procedure BFS(G, start):
                    create queue Q
                    mark start as visited
                    enqueue start into Q
                    while Q not empty:
                        v = dequeue Q
                        for each neighbor u of v:
                            if u not visited:
                                mark u visited
                                enqueue u
                """);
        Map<Integer, List<Integer>> graph = Map.of(
                0, List.of(1, 2),
                1, List.of(0, 3, 4),
                2, List.of(0, 5),
                3, List.of(1),
                4, List.of(1),
                5, List.of(2)
        );

        currentWorker = new SwingWorker<Void, String>()
        {
            @Override protected Void doInBackground()
            {
                boolean[] visited = new boolean[6];
                Queue<Integer> q = new LinkedList<>();
                visited[0] = true;
                q.add(0);
                publish("Start at node 0\n");
                try
                {
                    while (!q.isEmpty())
                    {
                        if (isCancelled()) throw new InterruptedException();
                        int v = q.poll();
                        publish("Processing: " + v + "\n");
                        highlightStep("process " + v);
                        sleepInterruptible(600);
                        for (int u : graph.getOrDefault(v, List.of()))
                        {
                            if (isCancelled()) throw new InterruptedException();
                            if (!visited[u])
                            {
                                visited[u] = true;
                                q.add(u);
                                publish("Enqueue: " + u + "\n");
                                highlightStep("enqueue " + u);
                                sleepInterruptible(400);
                            }
                        }
                    }
                } 
                catch (InterruptedException ex)
                {
                    publish("BFS interrupted.\n");
                }
                return null;
            }
            @Override protected void process(List<String> chunks)
            {
                for (String s : chunks) appendOutput(s);
            }
            @Override protected void done()
            {
                appendOutput("BFS completed.\n");
            }
        };
        currentWorker.execute();
    }

    // ---------- Linear Search ----------
    private void runLinearSearch()
    {
        setPseudocode("""
                LinearSearch(A, target):
                    for i = 0 to n-1:
                        if A[i] == target:
                            return i
                    return -1
                """);
        int[] arr = {2, 5, 8, 12, 16, 23, 38, 56, 72, 91};
        int target = 23;
        appendOutput("Array: " + Arrays.toString(arr) + "\n");
        appendOutput("Target: " + target + "\n");

        currentWorker = new SwingWorker<Void, String>()
        {
            @Override protected Void doInBackground()
            {
                try{
                    int result = -1;
                    for (int i = 0; i < arr.length; i++)
                    {
                        if (isCancelled()) throw new InterruptedException();
                        publish("Checking index " + i + ": A[" + i + "]=" + arr[i] + "\n");
                        highlightStep("Compare A[" + i + "] with target " + target);
                        sleepInterruptible(400);

                        if (arr[i] == target)
                        {
                            result = i;
                            break;
                        }
                    }

                    if (result != -1)
                    {
                        publish("\nTarget " + target + " found at index " + result + ".\n");
                    }
                    else
                    {
                        publish("\nTarget " + target + " not found.\n");
                    }
                }
                catch (InterruptedException ex)
                {
                    publish("Linear Search interrupted.\n");
                }
                return null;
            }
            @Override protected void process(List<String> chunks)
            {
                for (String s : chunks) appendOutput(s);
            }
            @Override protected void done()
            {
                appendOutput("Linear Search finished.\n");
            }
        };
        currentWorker.execute();
    }

    // ---------- Binary Search ----------
    private void runBinarySearch()
    {
        setPseudocode("""
                BinarySearch(A, target):
                    low = 0, high = n - 1
                    while low <= high:
                        mid = (low + high) / 2
                        if A[mid] == target: return mid
                        else if A[mid] < target: low = mid + 1
                        else: high = mid - 1
                    return -1
                """);
        int[] arr = {2, 5, 8, 12, 16, 23, 38, 56, 72, 91}; // Must be sorted
        int target = 56;
        appendOutput("Array (Sorted): " + Arrays.toString(arr) + "\n");
        appendOutput("Target: " + target + "\n");

        currentWorker = new SwingWorker<Void, String>()
        {
            @Override protected Void doInBackground()
            {
                try {
                    int low = 0;
                    int high = arr.length - 1;
                    int result = -1;

                    while (low <= high) 
                    {
                        if (isCancelled()) throw new InterruptedException();
                        int mid = low + (high - low) / 2; // Safer way to calculate mid

                        publish("\nRange: [" + low + ", " + high + "]. Mid index: " + mid + ", value: " + arr[mid] + "\n");
                        highlightStep("Check A[mid]=" + arr[mid]);
                        sleepInterruptible(600);

                        if (arr[mid] == target)
                        {
                            result = mid;
                            break;
                        } 
                        else if (arr[mid] < target) 
                        {
                            low = mid + 1;
                            publish("Target is greater, adjusting low to " + low + ".\n");
                            highlightStep("low = mid + 1");
                        } 
                        else 
                        {
                            high = mid - 1;
                            publish("Target is smaller, adjusting high to " + high + ".\n");
                            highlightStep("high = mid - 1");
                        }
                        sleepInterruptible(400);
                    }

                    if (result != -1) 
                    {
                        publish("\nTarget " + target + " found at index " + result + ".\n");
                    } 
                    else 
                    {
                        publish("\nTarget " + target + " not found.\n");
                    }
                } 
                catch (InterruptedException ex) 
                { 
                    publish("Binary Search interrupted.\n"); 
                }
                return null;
            }
            @Override protected void process(List<String> chunks) 
            {
                 for (String s : chunks) appendOutput(s);
            }
            @Override protected void done()
            { 
                appendOutput("Binary Search finished.\n"); 
            }
        };
        currentWorker.execute();
    }

    // ---------- Bubble Sort ----------
    private void runBubbleSort()
    {
        setPseudocode("""
                bubbleSort(A):
                    for i = 0 to n-2:
                        for j = 0 to n-2-i:
                            if A[j] > A[j+1]:
                                swap(A[j], A[j+1])
                """);
        int[] arr = {5, 1, 4, 2, 8};
        appendOutput("Original: " + Arrays.toString(arr) + "\n");

        currentWorker = new SwingWorker<Void, String>()
        {
            @Override protected Void doInBackground()
            {
                try
                {
                    int n = arr.length;
                    for (int i = 0; i < n - 1; i++)
                    {
                        for (int j = 0; j < n - 1 - i; j++)
                        {
                            if (isCancelled()) throw new InterruptedException();
                            publish("Compare A[" + j + "]=" + arr[j] + " and A[" + (j+1) + "]=" + arr[j+1] + "\n");
                            highlightStep("compare indices " + j + " and " + (j+1));
                            sleepInterruptible(350);
                            if (arr[j] > arr[j + 1])
                            {
                                int tmp = arr[j]; arr[j] = arr[j+1]; arr[j+1] = tmp;
                                publish("Swapped -> " + Arrays.toString(arr) + "\n");
                                highlightStep("swap positions " + j + " and " + (j+1));
                                sleepInterruptible(350);
                            }
                        }
                    }
                    publish("Bubble sort result: " + Arrays.toString(arr) + "\n");
                } 
                catch (InterruptedException ex) 
                {
                    publish("Bubble sort interrupted.\n"); 
                }
                return null;
            }
            @Override protected void process(List<String> chunks)
            { 
                for (String s : chunks) appendOutput(s);
            }
            @Override protected void done() 
            { 
                appendOutput("Bubble Sort completed.\n");
            }
        };
        currentWorker.execute();
    }

    // ---------- Quick Sort ----------
    private void runQuickSort()
    {
        setPseudocode("""
                quickSort(A, low, high):
                    if low < high:
                        pi = partition(A, low, high)
                        quickSort(A, low, pi-1)
                        quickSort(A, pi+1, high)
                partition(A, low, high):
                    pivot = A[high]
                    i = low - 1
                    for j = low to high-1:
                        if A[j] <= pivot:
                            i++
                            swap A[i], A[j]
                    swap A[i+1], A[high]
                    return i+1
                """);
        int[] arr = {10, 7, 8, 9, 1, 5};
        appendOutput("Original: " + Arrays.toString(arr) + "\n");

        currentWorker = new SwingWorker<Void, String>()
        {
            @Override protected Void doInBackground()
            {
                try{
                    quickSort(arr, 0, arr.length - 1);
                    publish("QuickSort result: " + Arrays.toString(arr) + "\n");
                    } 
                    catch (InterruptedException ex) 
                    { publish("QuickSort interrupted.\n"); }
                return null;
            }
            private void quickSort(int[] A, int low, int high) throws InterruptedException 
            {
                if (isCancelled()) throw new InterruptedException();
                if (low < high)
                {
                    int pi = partition(A, low, high);
                    highlightStep("pivot index: " + pi + " value: " + A[pi]);
                    quickSort(A, low, pi - 1);
                    quickSort(A, pi + 1, high);
                }
            }
            private int partition(int[] A, int low, int high) throws InterruptedException 
            {
                int pivot = A[high];
                publish("Partitioning with pivot A[" + high + "]=" + pivot + " on range [" + low + "," + high + "]\n");
                int i = low - 1;
                for (int j = low; j <= high - 1; j++)
                {
                    if (isCancelled()) throw new InterruptedException();
                    publish("Compare A[" + j + "]=" + A[j] + " <= pivot " + pivot + "?\n");
                    sleepInterruptible(300);
                    if (A[j] <= pivot)
                    {
                        i++;
                        int tmp = A[i]; A[i] = A[j]; A[j] = tmp;
                        publish("Swap -> " + Arrays.toString(A) + "\n");
                        sleepInterruptible(300);
                    }
                }
                int tmp = A[i+1]; A[i+1] = A[high]; A[high] = tmp;
                publish("Swap pivot to index " + (i+1) + " -> " + Arrays.toString(A) + "\n");
                return i + 1;
            }
            @Override protected void process(List<String> chunks)
            {
                for (String s : chunks) appendOutput(s);
            }
            @Override protected void done() 
            {
                 appendOutput("QuickSort finished.\n"); 
            }
        };
        currentWorker.execute();
    }

    // ---------- Merge Sort ----------
    private void runMergeSort()
    {
        setPseudocode("""
                mergeSort(A, l, r):
                    if l < r:
                        m = (l + r) / 2
                        mergeSort(A, l, m)
                        mergeSort(A, m+1, r)
                        merge(A, l, m, r)
                """);
        int[] arr = {12, 11, 13, 5, 6, 7};
        appendOutput("Original: " + Arrays.toString(arr) + "\n");

        currentWorker = new SwingWorker<Void, String>() 
        {
            @Override protected Void doInBackground() 
            {
                try {
                    mergeSort(arr, 0, arr.length - 1);
                    publish("MergeSort result: " + Arrays.toString(arr) + "\n");
                    }
                    catch (InterruptedException ex) 
                    { publish("MergeSort interrupted.\n"); }
                return null;
            }
            private void mergeSort(int[] A, int l, int r) throws InterruptedException 
            {
                if (isCancelled()) throw new InterruptedException();
                if (l < r) 
                {
                    int m = (l + r) / 2;
                    highlightStep("mergeSort range [" + l + "," + m + "]");
                    mergeSort(A, l, m);
                    highlightStep("mergeSort range [" + (m+1) + "," + r + "]");
                    mergeSort(A, m + 1, r);
                    merge(A, l, m, r);
                }
            }
            private void merge(int[] A, int l, int m, int r) throws InterruptedException 
            {
                int n1 = m - l + 1;
                int n2 = r - m;
                int[] L = new int[n1]; int[] R = new int[n2];
                for (int i = 0; i < n1; i++) L[i] = A[l + i];
                for (int j = 0; j < n2; j++) R[j] = A[m + 1 + j];
                publish("Merging: " + Arrays.toString(L) + " and " + Arrays.toString(R) + "\n");
                int i = 0, j = 0, k = l;
                while (i < n1 && j < n2)
                {
                    if (isCancelled()) throw new InterruptedException();
                    if (L[i] <= R[j]) { A[k++] = L[i++]; }
                    else { A[k++] = R[j++]; }
                    publish("Intermediate: " + Arrays.toString(A) + "\n");
                    sleepInterruptible(300);
                }
                while (i < n1) 
                { 
                    A[k++] = L[i++]; publish("Left remain: " + Arrays.toString(A) + "\n"); sleepInterruptible(200); 
                }
                while (j < n2) 
                { 
                    A[k++] = R[j++]; publish("Right remain: " + Arrays.toString(A) + "\n"); sleepInterruptible(200);
                }
                publish("After merge: " + Arrays.toString(A) + "\n");
            }
            @Override protected void process(List<String> chunks) 
            { for (String s : chunks) appendOutput(s); }
            @Override protected void done() 
            { appendOutput("MergeSort finished.\n"); }
        };
        currentWorker.execute();
    }

    // ---------- Heap Sort ----------
    private void runHeapSort()
    {
        setPseudocode("""
                heapsort(A):
                    build max heap
                    for i = n-1 down to 1:
                        swap A[0] and A[i]
                        heapify(A, i, 0)
                """);
        int[] arr = {12, 11, 13, 5, 6, 7};
        appendOutput("Original: " + Arrays.toString(arr) + "\n");

        currentWorker = new SwingWorker<Void, String>()
        {
            @Override protected Void doInBackground()
            {
                try
                {
                    int n = arr.length;
                    // build heap
                    for (int i = n / 2 - 1; i >= 0; i--)
                    {
                        if (isCancelled()) throw new InterruptedException();
                        heapify(arr, n, i);
                    }
                    publish("Max heap built: " + Arrays.toString(arr) + "\n");
                    // extract elements
                    for (int i = n - 1; i > 0; i--)
                    {
                        if (isCancelled()) throw new InterruptedException();
                        int tmp = arr[0]; arr[0] = arr[i]; arr[i] = tmp;
                        publish("Swapped root with index " + i + ": " + Arrays.toString(arr) + "\n");
                        highlightStep("swap root and A[" + i + "]");
                        sleepInterruptible(500);
                        heapify(arr, i, 0);
                    }
                    publish("Heapsort result: " + Arrays.toString(arr) + "\n");
                } 
                catch (InterruptedException ex)
                { 
                    publish("Heapsort interrupted.\n"); 
                }
                return null;
            }
            private void heapify(int[] A, int n, int i) throws InterruptedException
            {
                int largest = i, l = 2 * i + 1, r = 2 * i + 2;
                if (l < n && A[l] > A[largest]) largest = l;
                if (r < n && A[r] > A[largest]) largest = r;
                if (largest != i)
                {
                    int tmp = A[i]; A[i] = A[largest]; A[largest] = tmp;
                    publish("Swapped " + A[largest] + " and " + A[i] + "\n");
                    highlightStep("heapify swap at " + i);
                    sleepInterruptible(300);
                    if (isCancelled()) throw new InterruptedException();
                    heapify(A, n, largest);
                }
            }
            @Override protected void process(List<String> chunks)
            { 
                for (String s : chunks) appendOutput(s);
            }
            @Override protected void done()
            { 
                appendOutput("HeapSort completed.\n"); 
            }
        };
        currentWorker.execute();
    }

    // ---------- Build Heap (Max-Heap) ----------
    private void runBuildHeap()
    {
        setPseudocode("""
                procedure buildMaxHeap(A):
                    for i = floor(n/2)-1 down to 0:
                        heapify(A, n, i)
                procedure heapify(A, n, i):
                    largest = i
                    l = 2*i + 1
                    r = 2*i + 2
                    if l < n and A[l] > A[largest]: largest = l
                    if r < n and A[r] > A[largest]: largest = r
                    if largest != i:
                        swap(A[i], A[largest])
                        heapify(A, n, largest)
                """);
        int[] arr = {4, 10, 3, 5, 1, 8};
        appendOutput("Original: " + Arrays.toString(arr) + "\n");

        currentWorker = new SwingWorker<Void, String>()
        {
            @Override protected Void doInBackground()
            {
                int n = arr.length;
                try
                {
                    for (int i = n / 2 - 1; i >= 0; i--)
                    {
                        if (isCancelled()) throw new InterruptedException();
                        heapify(arr, n, i);
                        publish("After heapify(" + i + "): " + Arrays.toString(arr) + "\n");
                        highlightStep("heapify index " + i);
                        sleepInterruptible(600);
                    }
                    publish("Build Max-Heap result: " + Arrays.toString(arr) + "\n");
                } 
                catch (InterruptedException ex) 
                {
                  publish("Build heap interrupted.\n"); 
                }
                return null;
            }
            private void heapify(int[] A, int n, int i) throws InterruptedException
            {
                int largest = i, l = 2 * i + 1, r = 2 * i + 2;
                if (l < n && A[l] > A[largest]) largest = l;
                if (r < n && A[r] > A[largest]) largest = r;
                if (largest != i)
                {
                    int tmp = A[i]; A[i] = A[largest]; A[largest] = tmp;
                    publish("Swapped " + A[largest] + " and " + A[i] + "\n");
                    highlightStep("swap " + A[i] + " <-> " + A[largest]);
                    sleepInterruptible(400);
                    if (isCancelled()) throw new InterruptedException();
                    heapify(A, n, largest);
                }
            }
            @Override protected void process(List<String> chunks)
            {
                for (String s : chunks) appendOutput(s);
            }
            @Override protected void done() 
            {
                appendOutput("Heap construction done.\n");
            }
        };
        currentWorker.execute();
    }

    // ======= NEW ALGORITHMS IMPLEMENTED AS SWINGWORKERS =======

    // ---------- Stack Demo (LIFO) ----------
    private void runStackDemo()
    {
        setPseudocode("""
                Stack (LIFO):
                    Push(item): Add to top
                    Pop(): Remove from top
                    Peek(): Look at top
                """);
        currentWorker = new SwingWorker<Void, String>()
        {
            @Override protected Void doInBackground()
            {
                Stack<Integer> stack = new Stack<>();
                try{
                    // Push
                    for (int v : new int[]{10, 20, 30, 40})
                    {
                        if (isCancelled()) throw new InterruptedException();
                        stack.push(v);
                        publish("Pushed: " + v + ". Stack: " + stack + "\n");
                        highlightStep("Push(" + v + ")");
                        sleepInterruptible(500);
                    }

                    // Peek
                    if (isCancelled()) throw new InterruptedException();
                    publish("Peek: " + stack.peek() + "\n");
                    highlightStep("Peek()");
                    sleepInterruptible(500);

                    // Pop
                    while (!stack.isEmpty())
                    {
                        if (isCancelled()) throw new InterruptedException();
                        int x = stack.pop();
                        publish("Popped: " + x + ". Stack: " + stack + "\n");
                        highlightStep("Pop() -> " + x);
                        sleepInterruptible(500);
                    }
                } 
                catch (InterruptedException ex) 
                { publish("Stack Demo interrupted.\n"); }
                return null;
            }
            @Override protected void process(List<String> chunks)
            { for (String s : chunks) appendOutput(s); }
            @Override protected void done() 
            { appendOutput("Stack demo complete.\n"); }
        };
        currentWorker.execute();
    }

    // ---------- Queue Demo (FIFO) ----------
    private void runQueueDemo()
    {
        setPseudocode("""
                Queue (FIFO):
                    Enqueue(item): Add to back
                    Dequeue(): Remove from front
                    Peek(): Look at front
                """);
        currentWorker = new SwingWorker<Void, String>()
        {
            @Override protected Void doInBackground()
            {
                Queue<Integer> queue = new LinkedList<>();
                try{
                    // Enqueue
                    for (int v : new int[]{10, 20, 30, 40})
                    {
                        if (isCancelled()) throw new InterruptedException();
                        queue.add(v);
                        publish("Enqueued: " + v + ". Queue: " + queue + "\n");
                        highlightStep("Enqueue(" + v + ")");
                        sleepInterruptible(500);
                    }

                    // Peek
                    if (isCancelled()) throw new InterruptedException();
                    publish("Peek: " + queue.peek() + "\n");
                    highlightStep("Peek()");
                    sleepInterruptible(500);

                    // Dequeue
                    while (!queue.isEmpty())
                    {
                        if (isCancelled()) throw new InterruptedException();
                        int x = queue.poll();
                        publish("Dequeued: " + x + ". Queue: " + queue + "\n");
                        highlightStep("Dequeue() -> " + x);
                        sleepInterruptible(500);
                    }
                }
                catch (InterruptedException ex) 
                { publish("Queue Demo interrupted.\n"); }
                return null;
            }
            @Override protected void process(List<String> chunks)
            { for (String s : chunks) appendOutput(s); }
            @Override protected void done()
            { appendOutput("Queue demo complete.\n"); }
        };
        currentWorker.execute();
    }

    // ---------- Priority Queue Demo ----------
    private void runPriorityQueue()
    {
        setPseudocode("""
                create PriorityQueue PQ (max-heap)
                insert elements
                while PQ not empty:
                    remove highest priority
                """);
        currentWorker = new SwingWorker<Void, String>() 
        {
            @Override protected Void doInBackground()
            {
                PriorityQueue<Integer> pq = new PriorityQueue<>(Comparator.reverseOrder());
                int[] vals = {10, 5, 20, 1, 30};
                try
                {
                    for (int v : vals)
                    {
                        if (isCancelled()) throw new InterruptedException();
                        pq.add(v);
                        publish("Inserted: " + v + "\n");
                        highlightStep("insert " + v);
                        sleepInterruptible(400);
                    }
                    while (!pq.isEmpty())
                    {
                        if (isCancelled()) throw new InterruptedException();
                        int x = pq.poll();
                        publish("Polled highest priority: " + x + "\n");
                        highlightStep("poll -> " + x);
                        sleepInterruptible(500);
                    }
                } 
                catch (InterruptedException ex)
                {
                    publish("PQ demo interrupted.\n");
                }
                return null;
            }
            @Override protected void process(List<String> chunks)
            {
                for (String s : chunks) appendOutput(s);
            }
            @Override protected void done() 
            {
                appendOutput("PriorityQueue demo complete.\n"); 
            }
        };
        currentWorker.execute();
    }

    // ---------- Linked List Demo (Insert/Delete/Traverse) ----------
    private static class ListNode
    {
        int val; ListNode next;
        ListNode(int v) { val = v; next = null; }
        @Override public String toString() 
        { 
            return String.valueOf(val);
        }
    }

    private void runLinkedListDemo()
    {
        setPseudocode("""
                Linked List Operations:
                    Insertion (at head)
                    Traversal (Head to Tail)
                    Deletion (by value)
                """);

        currentWorker = new SwingWorker<Void, String>()
        {
            private ListNode head = null;

            private String listToString()
            {
                StringBuilder sb = new StringBuilder();
                ListNode current = head;
                while (current != null)
                {
                    sb.append(current.val);
                    if (current.next != null) sb.append(" -> ");
                    current = current.next;
                }
                return sb.toString();
            }

            @Override protected Void doInBackground()
            {
                try{
                    // Insertion at Head
                    publish("--- Insertion at Head ---\n");
                    for (int v : new int[]{3, 2, 1})
                    {
                        if (isCancelled()) throw new InterruptedException();
                        ListNode newNode = new ListNode(v);
                        newNode.next = head;
                        head = newNode;
                        publish("Inserted " + v + ". List: " + listToString() + "\n");
                        highlightStep("Insert(" + v + ") at head");
                        sleepInterruptible(500);
                    }

                    // Traversal
                    publish("\n--- Traversal ---\n");
                    ListNode current = head;
                    while (current != null)
                    {
                        if (isCancelled()) throw new InterruptedException();
                        publish("Visiting node: " + current.val + "\n");
                        highlightStep("Traverse: visit " + current.val);
                        current = current.next;
                        sleepInterruptible(500);
                    }

                    // Deletion (Delete 2)
                    publish("\n--- Deletion (Value 2) ---\n");
                    highlightStep("Delete(2)");
                    if (delete(2)) publish("Successfully deleted 2. List: " + listToString() + "\n");
                    else publish("Could not find 2 for deletion.\n");
                    sleepInterruptible(800);

                }
                catch (InterruptedException ex)
                { 
                    publish("Linked List Demo interrupted.\n");
                }
                return null;
            }

            private boolean delete(int key) throws InterruptedException 
            {
                ListNode temp = head, prev = null;

                // Case 1: Head is the key
                if (temp != null && temp.val == key)
                {
                    head = temp.next;
                    return true;
                }

                // Case 2: Search for key
                while (temp != null && temp.val != key)
                {
                    if (isCancelled()) throw new InterruptedException();
                    publish("Checking node: " + temp.val + "\n");
                    highlightStep("Delete: checking " + temp.val);
                    prev = temp;
                    temp = temp.next;
                    sleepInterruptible(300);
                }

                // If key was not present
                if (temp == null) return false;

                // Unlink the node
                if (prev != null)
                {
                    prev.next = temp.next;
                }
                return true;
            }

            @Override protected void process(List<String> chunks)
            {
                for (String s : chunks) appendOutput(s);
            }
            @Override protected void done() 
            { 
                appendOutput("Linked List demo complete.\n");
            }
        };
        currentWorker.execute();
    }

    // ---------- Binary Tree Traversals ----------
    private static class TreeNode
    {
        int val; TreeNode left, right;
        TreeNode(int v){ val = v; }
    }

    private TreeNode sampleTree()
    {
        // builds a sample tree:
        //        1
        //       / \
        //      2   3
        //     / \   \
        //    4   5   6
        TreeNode n1 = new TreeNode(1);
        TreeNode n2 = new TreeNode(2);
        TreeNode n3 = new TreeNode(3);
        TreeNode n4 = new TreeNode(4);
        TreeNode n5 = new TreeNode(5);
        TreeNode n6 = new TreeNode(6);
        n1.left = n2; n1.right = n3;
        n2.left = n4; n2.right = n5;
        n3.right = n6;
        return n1;
    }

    private void runTreeTraversal(String type)
    {
        String pseudocode;
        switch (type)
        {
            case "PREORDER" -> pseudocode = """
                    Preorder(node):
                        if node == null: return
                        visit(node)
                        Preorder(node.left)
                        Preorder(node.right)
                    """;
            case "INORDER" -> pseudocode = """
                    Inorder(node):
                        if node == null: return
                        Inorder(node.left)
                        visit(node)
                        Inorder(node.right)
                    """;
            default -> pseudocode = """
                    Postorder(node):
                        if node == null: return
                        Postorder(node.left)
                        Postorder(node.right)
                        visit(node)
                    """;
        }
        setPseudocode(pseudocode);
        TreeNode root = sampleTree();
        appendOutput("Tree: sample fixed tree (see code)\n");

        currentWorker = new SwingWorker<Void, String>()
        {
            @Override protected Void doInBackground()
            {
                try
                {
                    List<Integer> visited = new ArrayList<>();
                    if (type.equals("PREORDER")) preorder(root, visited);
                    else if (type.equals("INORDER")) inorder(root, visited);
                    else postorder(root, visited);
                    publish("Traversal sequence: " + visited + "\n");
                } 
                catch (InterruptedException ex)
                {
                    publish("Tree traversal interrupted.\n");
                }
                return null;
            }
            private void preorder(TreeNode node, List<Integer> list) throws InterruptedException
            {
                if (node == null) return;
                if (isCancelled()) throw new InterruptedException();
                list.add(node.val);
                publish("Visit: " + node.val + "\n");
                highlightStep("visit " + node.val);
                sleepInterruptible(500);
                preorder(node.left, list);
                preorder(node.right, list);
            }
            private void inorder(TreeNode node, List<Integer> list) throws InterruptedException
            {
                if (node == null) return;
                if (isCancelled()) throw new InterruptedException();
                inorder(node.left, list);
                if (isCancelled()) throw new InterruptedException();
                list.add(node.val);
                publish("Visit: " + node.val + "\n");
                highlightStep("visit " + node.val);
                sleepInterruptible(500);
                inorder(node.right, list);
            }
            private void postorder(TreeNode node, List<Integer> list) throws InterruptedException
            {
                if (node == null) return;
                if (isCancelled()) throw new InterruptedException();
                postorder(node.left, list);
                postorder(node.right, list);
                if (isCancelled()) throw new InterruptedException();
                list.add(node.val);
                publish("Visit: " + node.val + "\n");
                highlightStep("visit " + node.val);
                sleepInterruptible(500);
            }
            @Override protected void process(List<String> chunks)
            {
                for (String s : chunks) appendOutput(s);
            }
            @Override protected void done()
            { 
                appendOutput(type + " traversal finished.\n");
            }
        };
        currentWorker.execute();
    }
// ===== main (UNCHANGED) =====
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() ->
        {
            DSAVisualizerFull app = new DSAVisualizerFull();
            app.setVisible(true);
        });
    }
}