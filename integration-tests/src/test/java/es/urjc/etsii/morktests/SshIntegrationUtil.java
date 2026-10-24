package es.urjc.etsii.morktests;

import es.urjc.etsii.grafo.autoconfig.autollm.*;
import es.urjc.etsii.grafo.autoconfig.autollm.execution.RemoteWorkloadMain;
import java.io.File;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.jar.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/** Loopback-only SSH fixture, independent of the user's SSH keys and configuration. */
final class SshIntegrationUtil implements AutoCloseable {
    private final Path directory;
    private final Process server;
    final AutoLlmConfig.Ssh ssh;
    SshIntegrationUtil(Path directory) throws Exception {
        this.directory = directory; Files.createDirectories(directory);
        Path sshd = Path.of(System.getenv().getOrDefault("MORK_TEST_SSHD", "/usr/sbin/sshd")); assumeTrue(Files.isExecutable(sshd));
        Path host = directory.resolve("host"), key = directory.resolve("key");
        run(List.of("ssh-keygen", "-q", "-t", "ed25519", "-N", "", "-f", host.toString()));
        run(List.of("ssh-keygen", "-q", "-t", "ed25519", "-N", "", "-f", key.toString()));
        int port; try (var socket = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) { port = socket.getLocalPort(); }
        Path daemon = directory.resolve("sshd_config"), known = directory.resolve("known_hosts"), client = directory.resolve("ssh_config");
        Files.writeString(daemon, "Port " + port + "\nListenAddress 127.0.0.1\nHostKey " + host + "\nPidFile " + directory.resolve("pid") + "\nAuthorizedKeysFile " + key + ".pub\nStrictModes no\nUsePAM no\nPasswordAuthentication no\nKbdInteractiveAuthentication no\n");
        server = new ProcessBuilder(sshd.toString(), "-D", "-e", "-f", daemon.toString()).redirectErrorStream(true).redirectOutput(directory.resolve("sshd.log").toFile()).start(); Thread.sleep(200);
        if (!server.isAlive()) { server.destroyForcibly(); assumeTrue(false, "sshd unavailable: " + Files.readString(directory.resolve("sshd.log"))); }
        Files.writeString(known, "[127.0.0.1]:" + port + " " + Files.readString(Path.of(host + ".pub")));
        Files.writeString(client, "Host mork-integration\n HostName 127.0.0.1\n Port " + port + "\n User " + System.getProperty("user.name") + "\n IdentityFile " + key + "\n IdentitiesOnly yes\n StrictHostKeyChecking yes\n UserKnownHostsFile " + known + "\n");
        Path runner = directory.resolve("runner.jar"); var manifest = new Manifest(); manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0"); manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, RemoteWorkloadMain.class.getName());
        var paths = new ArrayList<String>(); for (String item : System.getProperty("java.class.path").split(File.pathSeparator)) paths.add(Path.of(item).toAbsolutePath().toUri().toString());
        manifest.getMainAttributes().put(Attributes.Name.CLASS_PATH, String.join(" ", paths));
        try (var jar = new JarOutputStream(Files.newOutputStream(runner), manifest)) { /* Fixture dependencies stay on the loopback host. */ }
        Path python = Path.of("../example-tsptw/venv/bin/python3").toAbsolutePath().normalize();
        assumeTrue(Files.isExecutable(python), "Prepare the instance-selector Python environment for SSH integration");
        ssh = new AutoLlmConfig.Ssh("server", runner.toString(), List.of(new AutoLlmConfig.Server("server", "mork-integration", directory.resolve("workloads").toString(), Path.of(System.getProperty("java.home"), "bin/java").toString(),
                Map.of("PATH", System.getenv("PATH"), "MORK_SELECTOR_PYTHON", python.toString(), "MPLCONFIGDIR", directory.resolve("matplotlib").toString()), client.toString(), null, null)));
    }
    private static void run(List<String> command) throws Exception {
        var process = new ProcessBuilder(command).redirectErrorStream(true).start(); String output = new String(process.getInputStream().readAllBytes());
        assertTrue(process.waitFor(10, TimeUnit.SECONDS)); assertEquals(0, process.exitValue(), output);
    }
    @Override public void close() throws Exception {
        // On failure, cancel only these fixture-owned jobs before stopping its SSH listener.
        Path workloads = directory.resolve("workloads");
        if (Files.exists(workloads)) try (var paths = Files.walk(workloads)) {
            for (Path file : paths.toList()) if (file.getFileName().toString().equals("started.json") && file.toString().contains("/.jobs/")) {
                Files.writeString(file.resolveSibling("cancel"), "fixture cleanup");
            }
        }
        server.destroy(); if (!server.waitFor(5, TimeUnit.SECONDS)) server.destroyForcibly();
    }
}
