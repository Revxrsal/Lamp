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
package revxrsal.commands.node;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.annotation.Named;

import java.lang.reflect.Parameter;
import java.util.Locale;

import static revxrsal.commands.util.BuiltInNamingStrategies.separateCamelCase;
import static revxrsal.commands.util.BuiltInNamingStrategies.upperCaseFirstLetter;

/**
 * An interface that allows customizing the way parameters are named
 * <p>
 * This also contains built-in naming strategies for common naming conventions,
 * such as upper camel case and lower-spaced case.
 * <p>
 * Note that a parameter that has {@link Named @Named} on it will not be passed
 * to the naming strategy, and will get its name from the annotation specified
 * on it.
 */
@FunctionalInterface
public interface ParameterNamingStrategy {

    /**
     * Preserves the default parameter name.
     *
     * @return The identity parameter naming strategy
     */
    static ParameterNamingStrategy identity() {
        return Parameter::getName;
    }

    /**
     * Converts the parameter name from lower-camel case to lower case,
     * separated by a custom separator.
     * <p>
     * This will capitalize the first character and separate words by the specified
     * string.
     * <ul>
     *   <li>someParameterName ---> some(separator)parameter(separator)name</li>
     *   <li>_someParameterName ---> _some(separator)parameter(separator)name</li>
     * </ul>
     *
     * @return The lower-camel case naming strategy
     */
    static ParameterNamingStrategy lowerCaseWithSeparator(String separator) {
        return parameter -> separateCamelCase(parameter.getName(), separator).toLowerCase(Locale.ENGLISH);
    }

    /**
     * Converts the parameter name from lower-camel case to lower case,
     * separated by a space.
     * <p>
     * This will capitalize the first character and separate words by a space.
     * <ul>
     *   <li>someParameterName ---> some parameter name</li>
     *   <li>_someParameterName ---> _some parameter name</li>
     * </ul>
     *
     * @return The lower-camel-with-space case naming strategy
     */
    static ParameterNamingStrategy lowerCaseWithSpace() {
        return lowerCaseWithSeparator(" ");
    }

    /**
     * Converts the parameter name from lower-camel case to upper-camel case.
     * This will simply capitalize the first character.
     * <ul>
     *  <li>someParameterName ---> SomeParameterName</li>
     *  <li>_someParameterName ---> _SomeParameterName</li>
     * </ul>
     *
     * @return The upper-camel case naming strategy
     */
    static ParameterNamingStrategy upperCamelCase() {
        return parameter -> upperCaseFirstLetter(parameter.getName());
    }

    /**
     * Converts the parameter name from lower-camel case to upper-camel case,
     * separated by spaces.
     * <p>
     * This will capitalize the first character and separate words by spaces.
     * <ul>
     *   <li>someParameterName ---> Some Parameter Name</li>
     *   <li>_someParameterName ---> _Some Parameter Name</li>
     * </ul>
     *
     * @return The upper-camel case naming strategy
     */
    static ParameterNamingStrategy upperCamelCaseWithSpace() {
        return upperCamelCaseWithSeparator(" ");
    }

    /**
     * Converts the parameter name from lower-camel case to upper-camel case,
     * separated by a custom separator.
     * <p>
     * This will capitalize the first character and separate words by the specified
     * string.
     * <ul>
     *   <li>someParameterName ---> Some(separator)Parameter(separator)Name</li>
     *   <li>_someParameterName ---> _Some(separator)Parameter(separator)Name</li>
     * </ul>
     *
     * @return The upper-camel case with custom separator naming strategy
     */
    static ParameterNamingStrategy upperCamelCaseWithSeparator(String separator) {
        return parameter -> upperCaseFirstLetter(separateCamelCase(parameter.getName(), separator));
    }

    /**
     * Returns the parameter name.
     *
     * @param parameter Parameter to get the name for
     * @return The parameter name
     */
    @NotNull String getName(@NotNull Parameter parameter);
}
