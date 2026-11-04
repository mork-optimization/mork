package es.urjc.etsii.grafo.autoconfig.autollm.execution;

/** PID and start time together identify a particular process, including after reconnect. */
public record ProcessIdentity(long pid, String start) {
    public static ProcessIdentity of(ProcessHandle process) {
        return new ProcessIdentity(process.pid(), process.info().startInstant().orElseThrow().toString());
    }
    public boolean alive() {
        var process = ProcessHandle.of(pid);
        if (process.isEmpty() || !process.get().isAlive()) return false;
        if (start == null || start.isBlank()) return true;
        return process.get().info().startInstant().map(i -> i.toString().equals(start)).orElse(true);
    }
}
