# How to contribute

Third party contributions are essential to improve the project. Not every contribution has to be a code change to the core framework, other contributions can be: documentation improvements, project examples, use cases, etc.

## Before contributing

* You may open an issue to discuss ideas with the community before starting to work on them. Early feedback can save tons of time in the long run.

### Code contributions to the core framework
* New code should have a test coverage of at least 80%.
* All new features and functionality changes must be documented: API methods using Javadocs, high level summary of functionality in the reference documentation (See `/docs`).


## Making Changes

* [Fork the repository](https://github.com/rmartinsanta/mork/fork).
* Create a topic branch from where you want to base your work.
  * This is usually the master branch.
  * To quickly create a topic branch based on master, run `git checkout -b
    fix_bug47 master`. Please avoid working directly on the
    `master` branch.
* Make sure you have added the necessary tests for your changes.
* Commits should include only a brief description in the first line, and may contain more information on the next lines. If the commits fixes a Github issue, please specify so (Example: `Fix #46: Wrong calculation in ...`).
* For details on how to run tests, please see [the quickstart guide](https://mork-optimization.readthedocs.io/en/latest/quickstart/starting/)

## Submitting Changes
* Push your changes to a topic branch in your fork of the repository.
* Submit a pull request to the main repository. Wait for the CI pipeline to execute. Both the docs and the source code will be tested.
* If all tests pass, the core team will soon review the pull request.
* After feedback has been given we expect responses within two weeks. After two
  weeks we may close the pull request if it isn't showing any activity.

## Additional Resources

* [Official documentation](https://mork-optimization.readthedocs.io)
* [Issues tracker](https://github.com/rmartinsanta/mork/issues)
* [CI / CD](https://github.com/rmartinsanta/mork/actions)
* [General GitHub documentation](https://help.github.com/)
* [GitHub pull request documentation](https://help.github.com/articles/creating-a-pull-request/)

Contributing guidelines inspired by [the Puppet project](https://github.com/puppetlabs/puppet).
