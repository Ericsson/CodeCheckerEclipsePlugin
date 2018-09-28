// -------------------------------------------------------------------------
//                     The CodeChecker Infrastructure
//   This file is distributed under the University of Illinois Open Source
//   License. See LICENSE.TXT for details.
// -------------------------------------------------------------------------

include "shared.thrift"

namespace py codeCheckerDBAccess_v6
namespace js codeCheckerDBAccess_v6
namespace java cc.codechecker.service.thrift.gen  

const i64 MAX_QUERY_SIZE = 500


/**
 * Detection status is an automated system which assigns a value to every
 * report during the storage process.
 */
enum DetectionStatus {
  NEW,         // The report appeared amongst the analysis results in the latest store.
  RESOLVED,    // The report has disappeared at the latest store.
  UNRESOLVED,  // The report has been seen multiple times in the previous stores, and it is still visible (not fixed).
  REOPENED     // The report has been RESOLVED in the past, but for some reason, appeared again.
}

enum DiffType {
  NEW,        // New (right) - Base (left)
  RESOLVED,   // Base (left) - New (right)
  UNRESOLVED  // Intersection of Base (left) and New (right)
}

enum Encoding {
  DEFAULT,
  BASE64
}

enum Order {
  ASC,
  DESC
}

/**
 * Review status is a feature which allows a user to assign one of these
 * statuses to a particular Report.
 */
enum ReviewStatus {
  UNREVIEWED,     // The report was not assigned a review (default).
  CONFIRMED,      // A user confirmed that this is a valid bug report.
  FALSE_POSITIVE, // A user confirmed that the bug is a false positive.
  INTENTIONAL     // A user confirmed that the bug is intentionally in the code.
}

/**
 * The severity of the reported issue. This list is generated from CodeChecker's
 * database on analyzer checkers.
 */
enum Severity {
  UNSPECIFIED   = 0,
  STYLE         = 10,
  LOW           = 20,
  MEDIUM        = 30,
  HIGH          = 40,
  CRITICAL      = 50
}

enum SortType {
  FILENAME,
  CHECKER_NAME,
  SEVERITY,
  REVIEW_STATUS,
  DETECTION_STATUS,
  BUG_PATH_LENGTH,
}

enum StoreLimitKind {
  FAILURE_ZIP_SIZE,         // Maximum size of the collected failed zips which can be store on the server.
  COMPILATION_DATABASE_SIZE // Limit of the compilation database file size.
}

struct SourceFileData {
  1: i64             fileId,
  2: string          filePath,
  3: optional string fileContent
}

struct SortMode {
  1: SortType type,
  2: Order    ord
}

struct BugPathEvent {
  1: i64    startLine,
  2: i64    startCol,
  3: i64    endLine,
  4: i64    endCol,
  5: string msg,
  6: i64    fileId
  7: string filePath
}
typedef list<BugPathEvent> BugPathEvents

struct BugPathPos {
  1: i64    startLine,
  2: i64    startCol,
  3: i64    endLine,
  4: i64    endCol,
  5: i64    fileId
  6: string filePath
}
typedef list<BugPathPos> BugPath

struct ReportDetails {
  1: BugPathEvents pathEvents,
  2: BugPath       executionPath
}

typedef string AnalyzerType

struct AnalyzerStatistics {
  1: string        version,         // Version information of the analyzer.
  2: i64           failed,          // Number of files which failed to analyze.
  3: i64           successful,      // Number of successfully analyzed files.
  4: list<string>  failedFilePaths, // List of file paths which failed to analyze.
}

typedef map<AnalyzerType, AnalyzerStatistics> AnalyzerStatisticsData

struct RunData {
  1: i64                       runId,                // Unique id of the run.
  2: string                    runDate,              // Date of the run last updated.
  3: string                    name,                 // Human-given identifier.
  4: i64                       duration,             // Duration of the run (-1 if not finished).
  5: i64                       resultCount,          // Number of unresolved results (review status is not FALSE_POSITIVE or INTENTIONAL) in the run.
  6: string                    runCmd,               // The used check command.
  7: map<DetectionStatus, i32> detectionStatusCount, // Number of reports with a particular detection status.
  8: string                    versionTag,           // Version tag of the latest run.
  9: string                    codeCheckerVersion,   // CodeChecker client version of the latest analysis.
  10: AnalyzerStatisticsData   analyzerStatistics,   // Statistics for each analyzers.
}
typedef list<RunData> RunDataList

struct RunHistoryData {
  1: i64                     runId,              // Unique id of the run.
  2: string                  runName,            // Name of the run.
  3: string                  versionTag,         // Version tag of the report.
  4: string                  user,               // User name who analysed the run.
  5: string                  time,               // Date time when the run was analysed.
  6: i64                     id,                 // Id of the run history tag.
  7: string                  checkCommand,       // Check command.
  8: string                  codeCheckerVersion, // CodeChecker client version of the latest analysis.
  9: AnalyzerStatisticsData  analyzerStatistics, // Statistics for analyzers.
}
typedef list<RunHistoryData> RunHistoryDataList

/**
 * Members of this struct are interpreted in "AND" relation with each other.
 * Between the list elements there is "OR" relation.
 * If exactMatch field is True it will use exact match for run names.
 */
struct RunHistoryFilter {
  1: list<string> tagNames,   // Part of the tag names.
}

struct RunTagCount {
  1: string          time,   // Date time of the last run.
  2: string          name,   // Name of the tag.
  3: i64             count,  // Count of the reports.
  4: i64             id,     // Id of the run tag.
  5: string          runName // Name of the run which the tag belongs to.
}
typedef list<RunTagCount> RunTagCounts

struct ReviewData {
  1: ReviewStatus  status,
  2: string        comment,
  3: string        author,
  4: string        date
}

struct ReportData {
  1: i64              runId,           // Unique id of the run.
  2: string           checkerId,       // The qualified name of the checker that reported this.
  3: string           bugHash,         // This is unique id of the concrete report.
  4: string           checkedFile,     // This is a filepath, the original main file the analyzer was called with.
  5: string           checkerMsg,      // Description of the bug report.
  6: i64              reportId,        // id of the report in the current run in the db.
  7: i64              fileId,          // Unique id of the file the report refers to.
  8: i64              line,            // line number or the reports main section (not part of the path).
  9: i64              column,          // column number of the report main section (not part of the path).
  10: Severity        severity,        // Checker severity.
  11: ReviewData      reviewData,      // Bug review status information.
  12: DetectionStatus detectionStatus, // State of the bug (see the enum constant values).
  13: string          detectedAt,      // Detection date of the report.
  14: string          fixedAt          // Date when the report was fixed.
  15: i64             bugPathLength,   // Length of the bug path.
}
typedef list<ReportData> ReportDataList

/**
 * Members of this struct are interpreted in "OR" relation with each other.
 * Between the elements of the list there is "AND" relation.
 */
struct ReportFilter {
  1: list<string>          filepath,
  2: list<string>          checkerMsg,
  3: list<string>          checkerName,
  4: list<string>          reportHash,
  5: list<Severity>        severity,
  6: list<ReviewStatus>    reviewStatus,
  7: list<DetectionStatus> detectionStatus,
  8: list<string>          runHistoryTag,      // Date of the run tag. !Deprecated!
  9: optional i64          firstDetectionDate,
  10: optional i64         fixDate,
  11: optional bool        isUnique,
  12: list<string>         runName,
  13: list<i64>            runTag,             // Ids of the run history tags.
  14: list<string>         componentNames,     // Names of the source components.
}

struct RunReportCount {
  1: i64            runId,        // Unique ID of the run.
  2: string         name,         // Human readable name of the run.
  3: i64            reportCount
}
typedef list<RunReportCount> RunReportCounts

struct CheckerCount {
  1: string      name,     // Name of the checker.
  2: Severity    severity, // Severity level of the checker.
  3: i64         count     // Number of reports.
}
typedef list<CheckerCount> CheckerCounts

struct CommentData {
  1: i64     id,
  2: string  author,
  3: string  message,
  4: string  createdAt
}
typedef list<CommentData> CommentDataList

/**
 * Members of this struct are interpreted in "AND" relation with each other.
 * Between the list elements there is "OR" relation.
 * If exactMatch field is True it will use exact match for run names.
 */
struct RunFilter {
  1: list<i64>    ids,        // IDs of the runs.
  2: list<string> names,      // Part of the run name.
  3: bool         exactMatch  // If it's True it will use an exact match for run names.
}

// CompareData is used as an optinal argument for multiple API calls.
// If not set the API calls will just simply filter or query the
// database for results or metrics.
// If set the API calls can be used in a compare mode where
// the results or metrics will be compared to the values set in the CompareData.
// In compare mode the baseline run ids should be set on the API
// (to what the results/metrics will be copared to) and the new run ids and the
// diff type should be set in the CompareData type.
struct CompareData {
  1: list<i64>  runIds,
  2: DiffType   diffType,
  3: list<i64>  runTag,    // Ids of the run history tags.
}

// This type is used to get line content information for the given file at the
// given positions.
struct LinesInFilesRequested {
  1: i64        fileId,
  2: set<i64>   lines
}
typedef list<LinesInFilesRequested> LinesInFilesRequestedList

struct SourceComponentData {
  1: string name,        // Name of the source component.
  2: string value,       // Value of the source component. Element of the value
                         // is separated by new lines. Each element begins
                         // with a `-` or a `+`, followed by a path glob pattern.
  3: string description, // Description of the source component.
}
typedef list<SourceComponentData> SourceComponentDataList

service codeCheckerDBAccess {

  // Gives back all analyzed runs.
  // PERMISSION: PRODUCT_ACCESS
  RunDataList getRunData(1: RunFilter runFilter)
                         throws (1: shared.RequestFailed requestError),

  // Get run history for runs.
  // If an empty run id list is provided the history
  // will be returned for all the available runs ordered by run history date.
  // PERMISSION: PRODUCT_ACCESS
  RunHistoryDataList getRunHistory(1: list<i64> runIds,
                                   2: i64       limit,
                                   3: i64       offset,
                                   4: RunHistoryFilter runHistoryFilter)
                                   throws (1: shared.RequestFailed requestError),

  // Returns report hashes based on the diffType parameter.
  // PERMISSION: PRODUCT_ACCESS
  list<string> getDiffResultsHash(1: list<i64>    runIds,
                                  2: list<string> reportHashes,
                                  3: DiffType     diffType)
                                  throws (1: shared.RequestFailed requestError),

  // PERMISSION: PRODUCT_ACCESS
  ReportData getReport(1: i64 reportId)
                       throws (1: shared.RequestFailed requestError),

  // Get the results for some runIds
  // can be used in diff mode if cmpData is set.
  // PERMISSION: PRODUCT_ACCESS
  ReportDataList getRunResults(1: list<i64>      runIds,
                               2: i64            limit,
                               3: i64            offset,
                               4: list<SortMode> sortType,
                               5: ReportFilter   reportFilter,
                               6: CompareData    cmpData)
                               throws (1: shared.RequestFailed requestError),


  // Count the results separately for multiple runs.
  // If an empty run id list is provided the report
  // counts will be calculated for all of the available runs.
  // PERMISSION: PRODUCT_ACCESS
  RunReportCounts getRunReportCounts(1: list<i64>    runIds,
                                     2: ReportFilter reportFilter,
                                     3: i64          limit,
                                     4: i64          offset)
                                     throws (1: shared.RequestFailed requestError),

  // Count all the results some runIds can be used for diff counting.
  // PERMISSION: PRODUCT_ACCESS
  i64 getRunResultCount(1: list<i64>    runIds,
                        2: ReportFilter reportFilter,
                        3: CompareData  cmpData)
                        throws (1: shared.RequestFailed requestError),

  // gives back the all marked region and message for a report
  // PERMISSION: PRODUCT_ACCESS
  ReportDetails getReportDetails(1: i64 reportId)
                                 throws (1: shared.RequestFailed requestError),

  // get file information, if fileContent is true the content of the source
  // file will be also returned
  // PERMISSION: PRODUCT_ACCESS
  SourceFileData getSourceFileData(1: i64      fileId,
                                   2: bool     fileContent,
                                   3: Encoding encoding)
                                   throws (1: shared.RequestFailed requestError),

  // Get line content information for multiple files in different positions.
  // The first key of the map is a file id, the second is a line number:
  // (e.g.: lineContent = result[fileId][line])
  // PERMISSION: PRODUCT_ACCESS
  map<i64, map<i64, string>> getLinesInSourceFileContents(1: LinesInFilesRequestedList linesInFilesRequested,
                                                          2: Encoding encoding)
                                                          throws (1: shared.RequestFailed requestError),

  // change review status of a bug.
  // PERMISSION: PRODUCT_ACCESS or PRODUCT_STORE
  bool changeReviewStatus(1: i64          reportId,
                          2: ReviewStatus status,
                          3: string       message)
                          throws (1: shared.RequestFailed requestError),

  // get comments for a bug
  // PERMISSION: PRODUCT_ACCESS
  CommentDataList getComments(1: i64 reportId)
                              throws(1: shared.RequestFailed requestError),

  // count all the comments for one bug
  // PERMISSION: PRODUCT_ACCESS
  i64 getCommentCount(1: i64 reportId)
                      throws(1: shared.RequestFailed requestError),

  // add new comment for a bug
  // PERMISSION: PRODUCT_ACCESS
  bool addComment(1: i64 reportId,
                  2: CommentData comment)
                  throws(1: shared.RequestFailed requestError),

  // update a comment
  // PERMISSION: PRODUCT_ACCESS
  bool updateComment(1: i64 commentId,
                     2: string newMessage)
                     throws(1: shared.RequestFailed requestError),

  // remove a comment
  // PERMISSION: PRODUCT_ACCESS
  bool removeComment(1: i64 commentId)
                     throws(1: shared.RequestFailed requestError),

  // get the md documentation for a checker
  string getCheckerDoc(1: string checkerId)
                       throws (1: shared.RequestFailed requestError),

  // returns the CodeChecker version that is running on the server
  string getPackageVersion();

  // remove bug results from the database
  // !!! DEPRECATED !!!
  // Use removeRun to remove the whole run or removeRunReports to remove
  // filtered run results.
  // PERMISSION: PRODUCT_STORE
  bool removeRunResults(1: list<i64> runIds)

  // remove bug results from the database
  // PERMISSION: PRODUCT_STORE
  bool removeRunReports(1: list<i64>    runIds,
                        2: ReportFilter reportFilter,
                        3: CompareData  cmpData)
                        throws (1: shared.RequestFailed requestError),

  // Remove run from the database.
  // PERMISSION: PRODUCT_STORE
  bool removeRun(1: i64 runId)
                 throws (1: shared.RequestFailed requestError),

  // get the suppress file path set by the command line
  // returns empty string if not set
  // PERMISSION: PRODUCT_ACCESS
  string getSuppressFile()
                        throws (1: shared.RequestFailed requestError),


  // If the run id list is empty the metrics will be counted
  // for all of the runs and in compare mode all of the runs
  // will be used as a baseline excluding the runs in compare data.
  // PERMISSION: PRODUCT_ACCESS
  map<Severity, i64> getSeverityCounts(1: list<i64>    runIds,
                                       2: ReportFilter reportFilter,
                                       3: CompareData  cmpData)
                                       throws (1: shared.RequestFailed requestError),

  // If the run id list is empty the metrics will be counted
  // for all of the runs and in compare mode all of the runs
  // will be used as a baseline excluding the runs in compare data.
  // PERMISSION: PRODUCT_ACCESS
  map<string, i64> getCheckerMsgCounts(1: list<i64>    runIds,
                                       2: ReportFilter reportFilter,
                                       3: CompareData  cmpData,
                                       4: i64          limit,
                                       5: i64          offset)
                                       throws (1: shared.RequestFailed requestError),

  // If the run id list is empty the metrics will be counted
  // for all of the runs and in compare mode all of the runs
  // will be used as a baseline excluding the runs in compare data.
  // PERMISSION: PRODUCT_ACCESS
  map<ReviewStatus, i64> getReviewStatusCounts(1: list<i64>    runIds,
                                               2: ReportFilter reportFilter,
                                               3: CompareData  cmpData)
                                               throws (1: shared.RequestFailed requestError),

  // If the run id list is empty the metrics will be counted
  // for all of the runs and in compare mode all of the runs
  // will be used as a baseline excluding the runs in compare data.
  // PERMISSION: PRODUCT_ACCESS
  map<DetectionStatus, i64> getDetectionStatusCounts(1: list<i64>    runIds,
                                                     2: ReportFilter reportFilter,
                                                     3: CompareData  cmpData)
                                                     throws (1: shared.RequestFailed requestError),

  // If the run id list is empty the metrics will be counted
  // for all of the runs and in compare mode all of the runs
  // will be used as a baseline excluding the runs in compare data.
  // PERMISSION: PRODUCT_ACCESS
  map<string, i64> getFileCounts(1: list<i64>    runIds,
                                 2: ReportFilter reportFilter,
                                 3: CompareData  cmpData,
                                 4: i64          limit,
                                 5: i64          offset)
                                 throws (1: shared.RequestFailed requestError),

  // If the run id list is empty the metrics will be counted
  // for all of the runs and in compare mode all of the runs
  // will be used as a baseline excluding the runs in compare data.
  // PERMISSION: PRODUCT_ACCESS
  CheckerCounts getCheckerCounts(1: list<i64>    runIds,
                                 2: ReportFilter reportFilter,
                                 3: CompareData  cmpData,
                                 4: i64          limit,
                                 5: i64          offset)
                                 throws (1: shared.RequestFailed requestError),

  // If the run id list is empty the metrics will be counted
  // for all of the runs and in compare mode all of the runs
  // will be used as a baseline excluding the runs in compare data.
  // PERMISSION: PRODUCT_ACCESS
  RunTagCounts getRunHistoryTagCounts(1: list<i64>    runIds,
                                      2: ReportFilter reportFilter,
                                      3: CompareData  cmpData)
                                      throws (1: shared.RequestFailed requestError),

  //============================================
  // Source component related API calls.
  //============================================

  // Add a new source component or override an existing one.
  // PERMISSION: PRODUCT_ADMIN
  bool addSourceComponent(1: string name,
                          2: string value,
                          3: string description)
                          throws (1: shared.RequestFailed requestError),

  // Get source components.
  // PERMISSION: PRODUCT_ACCESS
  SourceComponentDataList getSourceComponents(1: list<string> sourceComponentFilter)
                                              throws (1: shared.RequestFailed requestError),

  // Removes a source component.
  // PERMISSION: PRODUCT_ADMIN
  bool removeSourceComponent(1: string name)
                             throws (1: shared.RequestFailed requestError),

  //============================================
  // Analysis result storage related API calls.
  //============================================

  // The client can ask the server whether a file is already stored in the
  // database. If it is, then it is not necessary to send it in the ZIP file
  // with massStoreRun() function. This function requires a list of file hashes
  // (sha256) and returns the ones which are not stored yet.
  // PERMISSION: PRODUCT_STORE
  list<string> getMissingContentHashes(1: list<string> fileHashes)
                                       throws (1: shared.RequestFailed requestError),

  // This function stores an entire run encapsulated and sent in a ZIP file.
  // The ZIP file has to be compressed and sent as a base64 encoded string. The
  // ZIP file must contain a "reports" and an optional "root" sub-folder.
  // The former one is the output of 'CodeChecker analyze' command and the
  // latter one contains the source files on absolute paths starting as if
  // "root" was the "/" directory. The source files are not necessary to be
  // wrapped in the ZIP file (see getMissingContentHashes() function).
  //
  // The "version" parameter is the used CodeChecker version which checked this
  // run.
  // The "force" parameter removes existing analysis results for a run.
  // PERMISSION: PRODUCT_STORE
  i64 massStoreRun(1: string       runName,
                   2: string       tag,
                   3: string       version,
                   4: string       zipfile,
                   5: bool         force,
                   6: list<string> trimPathPrefixes)
                   throws (1: shared.RequestFailed requestError),

  // Returns true if analysis statistics information can be sent to the server,
  // otherwise it returns false.
  // PERMISSION: PRODUCT_STORE
  bool allowsStoringAnalysisStatistics()
                                       throws (1: shared.RequestFailed requestError),

  // Returns size limit for each server configuration parameter.
  // The first key of the map is the limit type, the second is the actual limit
  // value in bytes.
  // PERMISSION: PRODUCT_STORE
  map<StoreLimitKind, i64> getAnalysisStatisticsLimits()
                                                       throws (1: shared.RequestFailed requestError),

  // This function stores analysis statistics information on the server in a
  // directory which specified in the configuration file of the server. These
  // information are sent in a ZIP file where the ZIP file has to be compressed
  // and sent as a base64 encoded string.
  // PERMISSION: PRODUCT_STORE
  bool storeAnalysisStatistics(1: string runName
                               2: string zipfile)
                               throws (1: shared.RequestFailed requestError),
}
