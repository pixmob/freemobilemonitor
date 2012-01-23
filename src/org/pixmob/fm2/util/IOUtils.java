/*
 * Copyright (C) 2012 Pixmob (http://github.com/pixmob)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pixmob.fm2.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * I/0 utilities.
 * @author Pixmob
 */
public final class IOUtils {
    private IOUtils() {
    }
    
    /**
     * Quietly close a stream. This method accepts <code>null</code> values.
     * @param stream stream to close
     */
    public static void close(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException ignore) {
            }
        }
    }
    
    /**
     * Write a stream to a file.
     */
    public static void writeToFile(InputStream input, File outputFile)
            throws IOException {
        final FileOutputStream output = new FileOutputStream(outputFile);
        try {
            final byte[] buf = new byte[2048];
            for (int bytesRead; (bytesRead = input.read(buf)) != -1;) {
                output.write(buf, 0, bytesRead);
            }
        } finally {
            close(output);
            close(input);
        }
    }
}
