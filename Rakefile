require 'rake'
require 'io/console'

TMP_CHANGELOG_FILE = "/tmp/braintree-android-samsung-pay-release.md"

task :default => :unit_tests

desc "Run Android lint on all modules"
task :lint do
  sh "./gradlew clean lint :SamsungPay:assembleDebug :SamsungPay:assembleRelease"
end

desc "Run Android unit tests"
task :unit_tests => :lint do
  sh "./gradlew --continue test"
end

desc "Publish current version as a SNAPSHOT"
task :publish_snapshot => :unit_tests do
  abort("Version must contain '-SNAPSHOT'!") unless get_current_version.end_with?('-SNAPSHOT')

  puts "Ensure all tests are passing (`rake`)."
  $stdin.gets

  prompt_for_sonatype_username_and_password

  sh "./gradlew clean :SamsungPay:uploadArchives"
end

desc "Interactive release to publish new version"
task :release => :unit_tests do
  puts "Ensure all tests are passing (`rake`)."
  $stdin.gets

  puts "What version are you releasing? (x.x.x format)"
  version = $stdin.gets.chomp

  prompt_for_change_log(version)
  update_version(version)
  update_readme_version(version)

  prompt_for_sonatype_username_and_password

  Rake::Task["release_braintree_samsung_pay"].invoke

  post_release(version)
end

task :release_braintree_samsung_pay do
  sh "./gradlew clean :SamsungPay:uploadArchives"
  sh "./gradlew closeAndReleaseRepository"
  puts "Braintree Samsung Pay module have been released"
end

def prompt_for_sonatype_username_and_password
  puts "Enter Sonatype username:"
  ENV["SONATYPE_USERNAME"] = $stdin.gets.chomp

  puts "Enter Sonatype password:"
  ENV["SONATYPE_PASSWORD"] = $stdin.noecho(&:gets).chomp
end

def prompt_for_change_log(version)
  last_version = `git tag --sort=version:refname | tail -1`.chomp
  tmp_change_log = "#{version}"
  tmp_change_log += "\n\n# Please enter a summary of the changes below."
  tmp_change_log += "\n# Lines starting with '# ' will be ignored."
  tmp_change_log += "\n#"
  tmp_change_log += "\n# Changes since #{last_version}:"
  tmp_change_log += "\n#"
  tmp_change_log += "\n# "
  tmp_change_log += `git log --pretty=format:"%h %ad%x20%s%x20%x28%an%x29" --date=short #{last_version}..`.gsub("\n", "\n# ")
  tmp_change_log += "\n#"
  tmp_change_log += "\n"
  File.foreach("CHANGELOG.md") do |line|
    tmp_change_log += "# #{line}"
  end
  IO.write(TMP_CHANGELOG_FILE, tmp_change_log)

  puts "\n"
  sh "$EDITOR #{TMP_CHANGELOG_FILE}"

  new_changes = ""
  File.foreach(TMP_CHANGELOG_FILE) do |line|
    if !line.start_with?("# ") && !line.start_with?("#\n")
      new_changes += line
    end
  end

  IO.write("CHANGELOG.md",
    File.open("CHANGELOG.md") do |file|
      file.read.gsub("# Braintree Android Samsung Pay SDK Release Notes\n", "# Braintree Android Samsung Pay SDK Release Notes\n\n## #{new_changes.chomp}")
    end
  )
end

def post_release(version)
  if !`git remote`.include?("github")
    sh "git remote add github https://github.com/braintree/braintree-android-samsung-pay.git"
  end

  puts "\nArchives are uploaded! Committing and tagging #{version} and preparing for the next development iteration"
  sh "git commit -am 'Release #{version}'"
  sh "git tag -aF #{TMP_CHANGELOG_FILE} #{version}"

  version_values = version.split('.')
  version_values[2] = version_values[2].to_i + 1
  update_version("#{version_values.join('.')}-SNAPSHOT")
  update_readme_snapshot_version(version_values.join('.'))
  increment_version_code
  sh "git commit -am 'Prepare for development'"

  puts "\nDone. Commits and tags have been created. If everything appears to be in order, hit ENTER to push."
  $stdin.gets

  sh "git push origin master #{version}"

  puts "\nPushed to GHE! Press ENTER to push to public Github."
  $stdin.gets
end

def get_current_version
  current_version = nil
  File.foreach("build.gradle") do |line|
    if match = line.match(/versionName = '(\d+\.\d+\.\d+(-SNAPSHOT)?)'/)
      current_version = match.captures
    end
  end

  return current_version[0]
end

def increment_version_code
  new_build_file = ""
  File.foreach("build.gradle") do |line|
    if line.match(/versionCode = (\d+)/)
      new_build_file += line.gsub(/versionCode = \d+/, "versionCode = #{$1.to_i + 1}")
    else
      new_build_file += line
    end
  end
  IO.write('build.gradle', new_build_file)
end

def update_version(version)
  IO.write("build.gradle",
    File.open("build.gradle") do |file|
      file.read.gsub(/versionName = '\d+\.\d+\.\d+(-SNAPSHOT)?'/, "versionName = '#{version}'")
    end
  )
end

def update_readme_version(version)
  IO.write("README.md",
    File.open("README.md") do |file|
      file.read.gsub(/:samsung-pay:\d+\.\d+\.\d+'/, ":samsung-pay:#{version}'")
    end
  )
end

def update_readme_snapshot_version(snapshot_version)
  IO.write("README.md",
    File.open("README.md") do |file|
      file.read.gsub(/:samsung-pay:\d+\.\d+\.\d+-SNAPSHOT'/, ":samsung-pay:#{snapshot_version}-SNAPSHOT'")
    end
  )
end
