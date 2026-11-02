package es.urjc.etsii.grafo.autoconfig.autollm.execution;

import es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmFileUtil;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/** Campaign-owned reachable Git history. Never copies or modifies the original object database. */
public final class GitSourceStore {
    private GitSourceStore() {}
    @FunctionalInterface public interface Command {
        void run(List<String> arguments, Path directory, Path evidence);
    }

    public static synchronized Path bundle(Path store, Path repository, String commit, Path evidence, Command command) throws IOException {
        if (!commit.matches("[a-f0-9]{40,64}")) throw new IllegalArgumentException("Expected a full Git commit ID");
        store = store.toAbsolutePath().normalize();
        Files.createDirectories(store);
        Path bundle = store.resolve(commit + ".bundle"), receipt = store.resolve(commit + ".json");
        if (Files.isRegularFile(receipt)) {
            String expected = AutoLlmFileUtil.JSON.readTree(Files.readString(receipt)).path("sha256").asString();
            if (!Files.isRegularFile(bundle) || !AutoLlmFileUtil.hash(bundle).equals(expected)) throw new IOException("Source bundle changed: " + bundle);
            return bundle;
        }
        Path scratch = Files.createTempDirectory(store, "snapshot-");
        try {
            command.run(List.of("git", "init", "--bare", scratch.toString()), store, evidence.resolve("init"));
            command.run(List.of("git", "-c", "gc.auto=0", "fetch", "--no-tags", "--no-recurse-submodules", repository.toAbsolutePath().toString(), commit + ":refs/heads/baseline"), scratch, evidence.resolve("fetch"));
            Path temporary = scratch.resolve("snapshot.bundle");
            command.run(List.of("git", "bundle", "create", temporary.toString(), "refs/heads/baseline"), scratch, evidence.resolve("bundle"));
            command.run(List.of("git", "bundle", "verify", temporary.toString()), scratch, evidence.resolve("verify"));
            Files.move(temporary, bundle, StandardCopyOption.REPLACE_EXISTING);
            AutoLlmFileUtil.write(receipt, Map.of("version", 1, "commit", commit, "sha256", AutoLlmFileUtil.hash(bundle)));
            return bundle;
        } finally { WorkloadArchiveUtil.deleteTree(scratch); }
    }

    public static void checkout(Path store, Path repository, String commit, Path destination, Path evidence, Command command) throws IOException {
        Path bundle = bundle(store, repository, commit, evidence.resolve("snapshot"), command);
        if (!Files.exists(destination.resolve(".git"))) {
            Files.createDirectories(destination.toAbsolutePath().getParent());
            command.run(List.of("git", "clone", "--no-checkout", bundle.toString(), destination.toAbsolutePath().toString()), store, evidence.resolve("clone"));
            // Preserve relative submodule URL resolution without borrowing the original object database.
            command.run(List.of("git", "remote", "set-url", "origin", repository.toAbsolutePath().toString()), destination, evidence.resolve("origin"));
        }
        command.run(List.of("git", "-c", "core.autocrlf=false", "checkout", "--detach", commit), destination, evidence.resolve("checkout"));
        command.run(List.of("git", "-c", "core.autocrlf=false", "submodule", "update", "--init", "--recursive"), destination, evidence.resolve("submodules"));
    }
}
