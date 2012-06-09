class AttachScreenshotController < ApplicationController
  unloadable
  skip_before_filter :check_if_login_required
  skip_before_filter :verify_authenticity_token
  
  # Redmine 1.3 and prior use accept_key_auth to enforce auth 
  # via the key we hand to the applet.
  if Redmine::VERSION::MAJOR == 1 and Redmine::VERSION::MINOR <= 3 then
    accept_key_auth :index
  else
    accept_api_auth :index
  end

  def index
    if Gem::Version.new("3.0") > Gem::Version.new(Rails.version) then
      path = "#{RAILS_ROOT}/tmp/"
    else
      path = "#{Rails.root}/tmp/"
    end
    if request.post?
      date = DateTime.now.strftime("%H%M%S")
      @fname = make_tmpname(date)
      file = File.new(path + make_tmpname(date), "wb");
      file.write(params[:attachments].read);
      file.close();
      if (Object.const_defined?(:Magick))
        img = Magick::Image::read(file.path()).first
        thumb = img.resize_to_fit(150, 150)
        @fname = make_tmpname(date, "thumb.png")
        thumb.write path + @fname
      end
        render :inline => "<%= @fname %>"
    else
      @fname = params[:id];
      send_file(path + @fname,
                :disposition => 'inline',
                :type => 'image/png',
                :filename => "screenshot.png");
    end
  end

  private

  def make_tmpname(date, name = "screenshot.png")
    sprintf('%d_%f%s', User.current.id, date, name)
  end
end
