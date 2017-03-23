package com.nytimes.android.external.fs2;

import com.nytimes.android.external.fs2.filesystem.FileSystem;
import com.nytimes.android.external.store.base.DiskRead;

import java.io.FileNotFoundException;

import javax.annotation.Nonnull;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import okio.BufferedSource;

/**
 * FSReader is used when persisting from file system
 * PathResolver will be used in creating file system paths based on cache keys.
 * Make sure to have keys containing same data resolve to same "path"
 *
 * @param <T> key type
 */
public class FSReader<T> implements DiskRead<BufferedSource, T> {
    final FileSystem fileSystem;
    final PathResolver<T> pathResolver;

    public FSReader(FileSystem fileSystem, PathResolver<T> pathResolver) {
        this.fileSystem = fileSystem;
        this.pathResolver = pathResolver;
    }

    @Nonnull
    @Override
    public Observable<BufferedSource> read(@Nonnull final T key) {
        return Observable.create(new ObservableOnSubscribe<BufferedSource>() {
            @Override
            public void subscribe(ObservableEmitter<BufferedSource> emitter) {
                String resolvedKey = pathResolver.resolve(key);
                boolean exists = fileSystem.exists(resolvedKey);

                if (exists) {
                    try {
                        BufferedSource bufferedSource = fileSystem.read(resolvedKey);
                        emitter.onNext(bufferedSource);
                        emitter.onComplete();
                    } catch (FileNotFoundException e) {
                        emitter.onError(e);
                    }
                } else {
                    emitter.onComplete();
                }
            }
        });
    }
}