package es.urjc.etsii.grafo.autoconfig.autollm.execution;

import es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmFileUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

class GitSourceStoreTest {
    @TempDir Path root;
    private void command(List<String> args, Path cwd, Path logs) {
        try {
            Files.createDirectories(logs);
            var process = new ProcessBuilder(args).directory(cwd.toFile()).redirectError(logs.resolve("stderr.log").toFile()).redirectOutput(logs.resolve("stdout.log").toFile()).start();
            assertTrue(process.waitFor(30, TimeUnit.SECONDS));
            assertEquals(0, process.exitValue(), () -> {
                try { return Files.readString(logs.resolve("stdout.log")) + Files.readString(logs.resolve("stderr.log")); } catch (Exception e) { return e.toString(); }
            });
        } catch (Exception e) { throw new RuntimeException(e); }
    }
    @Test void snapshotsExcludeGarbageAndRemainIndependentOfOriginalRepository() throws Exception {
        Path repository = root.resolve("original repo"), logs = root.resolve("logs"), store = root.resolve("store");
        Files.createDirectories(repository);
        command(List.of("git", "init", repository.toString()), root, logs);
        Files.writeString(repository.resolve("source.txt"), "baseline\n");
        command(List.of("git", "add", "source.txt"), repository, logs);
        command(List.of("git", "-c", "user.name=Test", "-c", "user.email=test@example.invalid", "commit", "-m", "baseline"), repository, logs);
        command(List.of("git", "rev-parse", "HEAD"), repository, logs);
        String commit = Files.readString(logs.resolve("stdout.log")).strip();
        Files.writeString(repository.resolve(".git/objects/tmp_obj_garbage"), "unrelated garbage");
        Path first = root.resolve("first"), second = root.resolve("second");
        GitSourceStore.checkout(store, repository, commit, first, logs, this::command);
        assertFalse(Files.exists(first.resolve(".git/objects/tmp_obj_garbage")));
        Path bundle = store.resolve(commit + ".bundle");
        String hash = AutoLlmFileUtil.hash(bundle);
        Files.move(repository, root.resolve("original moved"));
        GitSourceStore.checkout(store, repository, commit, second, logs, this::command);
        assertEquals("baseline\n", Files.readString(second.resolve("source.txt")));
        Files.writeString(first.resolve("source.txt"), "modified");
        assertEquals("baseline\n", Files.readString(second.resolve("source.txt")));
        assertEquals(hash, AutoLlmFileUtil.hash(bundle));
        Files.writeString(bundle, "corrupt");
        assertThrows(java.io.IOException.class, () -> GitSourceStore.checkout(store, repository, commit, root.resolve("third"), logs, this::command));
    }

    @Test void binaryPatchRestoresAdditionsChangesAndDeletionsWithoutTouchingSourceIndex() throws Exception {
        Path repository = root.resolve("original"), logs = root.resolve("logs"), store = root.resolve("store");
        Files.createDirectories(repository);
        command(List.of("git", "init", repository.toString()), root, logs);
        Files.writeString(repository.resolve("keep.txt"), "before\n");
        Files.writeString(repository.resolve("deleted.txt"), "remove me");
        command(List.of("git", "add", "."), repository, logs);
        command(List.of("git", "-c", "user.name=Test", "-c", "user.email=test@example.invalid", "commit", "-m", "baseline"), repository, logs);
        command(List.of("git", "rev-parse", "HEAD"), repository, logs);
        String commit = Files.readString(logs.resolve("stdout.log")).strip();
        Path bundle = GitSourceStore.bundle(store, repository, commit, logs, this::command);
        Files.writeString(repository.resolve("keep.txt"), "after\n");
        Files.delete(repository.resolve("deleted.txt"));
        byte[] binary = new byte[] {0, 1, 2, -1, 0};
        Files.write(repository.resolve("added.bin"), binary);
        String index = AutoLlmFileUtil.hash(repository.resolve(".git/index"));
        Path snapshot = root.resolve("snapshot");
        GitPatchSnapshot.capture(bundle, commit, repository, snapshot, this::command);
        assertEquals(index, AutoLlmFileUtil.hash(repository.resolve(".git/index")));
        Path restored = root.resolve("restored");
        GitPatchSnapshot.restore(bundle, snapshot, restored, this::command);
        assertEquals("after\n", Files.readString(restored.resolve("keep.txt")));
        assertFalse(Files.exists(restored.resolve("deleted.txt")));
        assertArrayEquals(binary, Files.readAllBytes(restored.resolve("added.bin")));
        Files.writeString(snapshot.resolve("source.patch"), "corruption");
        assertThrows(java.io.IOException.class, () -> GitPatchSnapshot.restore(bundle, snapshot, root.resolve("corrupt"), this::command));
    }

    private String initialize(Path repository) throws Exception {
        Files.createDirectories(repository);
        Path logs = root.resolve("init-logs");
        command(List.of("git", "init", repository.toString()), root, logs);
        Files.writeString(repository.resolve("file.txt"), "baseline\n");
        command(List.of("git", "add", "."), repository, logs);
        command(List.of("git", "-c", "user.name=Test", "-c", "user.email=test@example.invalid", "commit", "-m", "baseline"), repository, logs);
        command(List.of("git", "rev-parse", "HEAD"), repository, logs);
        return Files.readString(logs.resolve("stdout.log")).strip();
    }
    @Test void preservesIgnoredBuildInputsAndRawLineEndingsButExcludesBuildCaches() throws Exception {
        Path repository = root.resolve("original"); String commit = initialize(repository);
        Files.writeString(repository.resolve(".gitignore"), "*.input\ntarget/\n");
        Files.writeString(repository.resolve(".gitattributes"), "*.txt text eol=lf\n");
        Files.writeString(repository.resolve("file.txt"), "windows\r\nbytes\r\n");
        Files.writeString(repository.resolve("required.input"), "ignored build input");
        Files.createDirectories(repository.resolve("target")); Files.writeString(repository.resolve("target/cache"), "disposable");
        Path store = root.resolve("store"), logs = root.resolve("logs"), snapshot = root.resolve("snapshot");
        Path bundle = GitSourceStore.bundle(store, repository, commit, logs, this::command);
        GitPatchSnapshot.capture(bundle, commit, repository, snapshot, this::command);
        GitPatchSnapshot.restore(bundle, snapshot, root.resolve("restored"), this::command);
        assertEquals("windows\r\nbytes\r\n", Files.readString(root.resolve("restored/file.txt")));
        assertEquals("ignored build input", Files.readString(root.resolve("restored/required.input")));
        assertFalse(Files.exists(root.resolve("restored/target")));
    }
    @Test void restoresDirtySubmodulesAfterOriginalRepositoriesDisappear() throws Exception {
        Path child = root.resolve("child"), repository = root.resolve("parent"), logs = root.resolve("logs");
        Path grandchild = root.resolve("grandchild"); initialize(grandchild);
        initialize(child); initialize(repository);
        command(List.of("git", "-c", "protocol.file.allow=always", "submodule", "add", grandchild.toString(), "nested"), child, logs);
        command(List.of("git", "-c", "user.name=Test", "-c", "user.email=test@example.invalid", "commit", "-am", "nested submodule"), child, logs);
        command(List.of("git", "-c", "protocol.file.allow=always", "submodule", "add", child.toString(), "module with spaces"), repository, logs);
        command(List.of("git", "-c", "user.name=Test", "-c", "user.email=test@example.invalid", "commit", "-am", "submodule"), repository, logs);
        command(List.of("git", "rev-parse", "HEAD"), repository, logs);
        String commit = Files.readString(logs.resolve("stdout.log")).strip();
        command(List.of("git", "-c", "protocol.file.allow=always", "submodule", "update", "--init", "--recursive"), repository, logs);
        Files.writeString(repository.resolve("module with spaces/nested/file.txt"), "dirty nested submodule");
        Files.writeString(repository.resolve("module with spaces/file.txt"), "dirty submodule\n");
        Files.writeString(repository.resolve("module with spaces/new.txt"), "new child input");
        Path bundle = GitSourceStore.bundle(root.resolve("store"), repository, commit, logs, this::command);
        Path snapshot = root.resolve("snapshot");
        GitPatchSnapshot.capture(bundle, commit, repository, snapshot, this::command);
        Files.move(repository, root.resolve("parent moved")); Files.move(child, root.resolve("child moved")); Files.move(grandchild, root.resolve("grandchild moved"));
        GitPatchSnapshot.restore(bundle, snapshot, root.resolve("restored"), this::command);
        assertEquals("dirty submodule\n", Files.readString(root.resolve("restored/module with spaces/file.txt")));
        assertEquals("new child input", Files.readString(root.resolve("restored/module with spaces/new.txt")));
        assertEquals("dirty nested submodule", Files.readString(root.resolve("restored/module with spaces/nested/file.txt")));
        var manifest = AutoLlmFileUtil.read(snapshot.resolve("snapshot.json"), GitPatchSnapshot.Manifest.class);
        Path childManifest = snapshot.resolve(manifest.submodules().get("module with spaces").snapshot()).resolve("snapshot.json");
        Files.writeString(childManifest, "changed child manifest");
        assertThrows(java.io.IOException.class, () -> GitPatchSnapshot.restore(bundle, snapshot, root.resolve("tampered-child"), this::command));
        assertFalse(Files.exists(root.resolve("tampered-child")));
    }

    @Test void frozenCampaignSourcesRestoreAfterRelocationWithoutOriginalCheckout() throws Exception {
        Path repository = root.resolve("original"), campaign = root.resolve("campaign");
        String commit = initialize(repository);
        Files.writeString(repository.resolve("added.txt"), "generated component");
        Path trial = campaign.resolve("trials/001"), attempt = trial.resolve("attempts/001-preparation");
        Files.createDirectories(attempt);
        var config = es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmConfig.defaults();
        var record = es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmSourceService.freeze(campaign, trial, repository, attempt, commit, config, Map.of("java", "fixture"), this::command);
        assertFalse(Path.of(record.snapshot()).isAbsolute());
        assertTrue(Files.exists(campaign.resolve(record.snapshot()).resolve("verified.json")));
        Path moved = root.resolve("moved campaign"); Files.move(campaign, moved); Files.move(repository, root.resolve("original moved"));
        Path destination = root.resolve("exported sources");
        es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmSourceService.restore(moved, moved.resolve("trials/001"), destination, this::command);
        assertEquals("generated component", Files.readString(destination.resolve("added.txt")));
        assertThrows(java.io.IOException.class, () -> es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmSourceService.restore(moved, moved.resolve("trials/001"), destination, this::command));
        assertEquals("generated component", Files.readString(destination.resolve("added.txt")));
    }
}
