/*
 * This file is part of lamp, licensed under the MIT License.
 *
 *  Copyright (c) Revxrsal <reflxction.github@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */
package revxrsal.commands.jda.exception;

import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

/**
 * Thrown when the user supplies a channel of the wrong type
 */
public class WrongChannelTypeException extends RuntimeException {

    /**
     * The channel the user supplied
     */
    private final GuildChannel channel;

    /**
     * The channel type we want
     */
    private final Class<?> expectedType;

    public WrongChannelTypeException(GuildChannel channel, Class<?> expectedType) {
        this.channel = channel;
        this.expectedType = expectedType;
    }

    /**
     * Returns the channel supplied by the user
     *
     * @return the channel supplied by the user
     */
    public GuildChannel channel() {
        return channel;
    }

    /**
     * Returns the type we want
     *
     * @return The type we want
     */
    public Class<?> expectedType() {
        return expectedType;
    }
}
