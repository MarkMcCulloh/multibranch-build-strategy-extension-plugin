package com.mmcculloh.jenkins.plugins.multibranch.pathextensions;

import hudson.plugins.git.GitChangeLogParser;
import hudson.plugins.git.GitChangeSet;
import hudson.plugins.git.GitChangeSet.Path;
import hudson.scm.SCM;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import jenkins.branch.BranchBuildStrategy;
import jenkins.plugins.git.AbstractGitSCMSource;
import jenkins.plugins.git.GitSCMFileSystem;
import jenkins.scm.api.SCMFileSystem;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceOwner;

public abstract class BranchBuildStrategyExtension extends BranchBuildStrategy {
  private static final int HASH_LENGTH = 40;
  private static final Logger logger = Logger.getLogger(BranchBuildStrategyExtension.class.getName());

  protected SCMFileSystem buildSCMFileSystem(SCMSource source, SCMHead head, SCMRevision currRevision, SCM scm,
      SCMSourceOwner owner) throws Exception {
    GitSCMFileSystem.Builder builder = new GitSCMFileSystem.BuilderImpl();
    if (currRevision != null && !(currRevision instanceof AbstractGitSCMSource.SCMRevisionImpl))
      return builder.build(source, head,
          new AbstractGitSCMSource.SCMRevisionImpl(head, currRevision.toString().substring(0, 40)));
    else
      return builder.build(owner, scm, currRevision);
  }

  protected List<GitChangeSet> getGitChangeSetListFromPrevious(SCMFileSystem fileSystem, SCMHead head,
      SCMRevision prevRevision) throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    if (prevRevision != null && !(prevRevision instanceof AbstractGitSCMSource.SCMRevisionImpl)) {
      fileSystem.changesSince(
          new AbstractGitSCMSource.SCMRevisionImpl(head, prevRevision.toString().substring(0, HASH_LENGTH)), out);
    } else {
      fileSystem.changesSince(prevRevision, out);
    }
    GitChangeLogParser parser = new GitChangeLogParser(true);
    return parser.parse(new ByteArrayInputStream(out.toByteArray()));
  }

  protected Set<String> collectAllAffectedFiles(List<GitChangeSet> gitChangeSetList) {
    Set<String> pathesSet = new HashSet<String>();
    for (GitChangeSet gitChangeSet : gitChangeSetList) {
      List<Path> affectedFilesList = new ArrayList<Path>(gitChangeSet.getAffectedFiles());
      for (Path path : affectedFilesList) {
        pathesSet.add(path.getPath());
        logger.fine("File:" + path.getPath() + " from commit:" + gitChangeSet.getCommitId() + " Change type:"
            + path.getEditType().getName());
      }
    }
    return pathesSet;
  }

  protected Set<String> collectAllComments(List<GitChangeSet> gitChangeSetList) {
    Set<String> comments = new HashSet<String>();
    for (GitChangeSet gitChangeSet : gitChangeSetList) {
      comments.add(gitChangeSet.getComment());
    }
    return comments;
  }
}
