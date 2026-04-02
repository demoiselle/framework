/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.api.crud;

import java.util.List;

/**
 * Simple stub implementation of {@link Result} for testing Crud default methods.
 */
class StubResult<T> implements Result<T> {

    private List<T> content;

    StubResult(List<T> content) {
        this.content = content;
    }

    @Override
    public List<T> getContent() {
        return content;
    }

    @Override
    public void setContent(List<T> content) {
        this.content = content;
    }
}
