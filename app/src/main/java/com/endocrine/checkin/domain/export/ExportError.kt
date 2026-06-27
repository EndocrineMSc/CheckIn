package com.endocrine.checkin.domain.export

import com.endocrine.checkin.domain.util.Error

/** Why a CSV export failed. Local-only, so the set is small. */
enum class ExportError : Error {
    /** The SAF Uri could not be opened for writing (revoked, missing, etc.). */
    CANNOT_OPEN_TARGET,

    /** Writing the bytes failed (e.g. disk full, I/O error). */
    WRITE_FAILED,

    UNKNOWN,
}
