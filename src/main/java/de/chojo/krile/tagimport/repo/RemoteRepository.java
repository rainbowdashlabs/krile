package de.chojo.krile.tagimport.repo;

import de.chojo.krile.data.dao.Identifier;
import org.eclipse.jgit.api.Git;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public class RemoteRepository extends RawRepository implements Closeable {
    protected RemoteRepository(String url, Identifier identifier, Path root, Git git) {
        super(url, identifier, root, git);
    }

    @Override
    public void close() throws IOException {
        git().close();
        try (var stream = Files.walk(root())) {
            stream.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

}
